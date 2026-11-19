const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";
const ADMIN_EMAIL = "suryakantabarik216@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("subjects").where("boardId", "==", "BSE_10").where("published", "==", true).get());
  await assertFails(unauthDb.collection("mcqs").where("boardId", "==", "BSE_10").where("published", "==", true).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Unauth",
      boardId: "BSE_10",
      language: "EN",
      examDateEpochDays: 20900,
      dailyTargetQuestions: 20,
      streakDays: 0,
      lastStudyEpochDay: 20775,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Student user profile: valid create/update, cross-user denial, and ghost field rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aarav",
      boardId: "BSE_10",
      language: "OR",
      examDateEpochDays: 20900,
      dailyTargetQuestions: 25,
      streakDays: 1,
      lastStudyEpochDay: 20775,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update preserving createdAt
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      language: "EN",
      dailyTargetQuestions: 30,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / role escalation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Future",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Cross-user read & write must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User bookmarks, chapter progress, and attempts: isolation and terminal status lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("bm_1").set({
      itemId: "mcq_1",
      itemType: "MCQ",
      boardId: "BSE_10",
      subjectId: "bse_math",
      inRevisionList: true,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookmarks").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chapter_progress").doc("ch_1").set({
      chapterId: "ch_1",
      boardId: "BSE_10",
      subjectId: "bse_math",
      completed: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Quiz attempt: IN_PROGRESS -> COMPLETED -> locked against further student updates
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("attempts").doc("att_1").set({
      boardId: "BSE_10",
      subjectId: "bse_math",
      mode: "QUIZ",
      title: "Math Quiz 10",
      totalQuestions: 10,
      correctCount: 4,
      wrongCount: 1,
      scoreMarks: 4,
      maxMarks: 10,
      durationSeconds: 120,
      status: "IN_PROGRESS",
      wrongMcqIds: ["mcq_2"],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("attempts").doc("att_1").update({
      correctCount: 8,
      wrongCount: 2,
      scoreMarks: 8,
      durationSeconds: 310,
      status: "COMPLETED",
      wrongMcqIds: ["mcq_2", "mcq_7"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: student cannot tamper with completed score
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("attempts").doc("att_1").update({
      scoreMarks: 10,
      correctCount: 10,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Catalog RBAC: Admin writes succeed, non-admin writes and email spoof fail, published filter enforced", async () => {
  const now = new Date(Date.now() - 5000);

  // Spoofed admin email with email_verified=false must be rejected
  const spoofDb = testEnv.authenticatedContext("spoof_1", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofDb.collection("subjects").doc("sub_spoof").set({
      boardId: "BSE_10",
      nameEn: "Spoof",
      nameOr: "ସ୍ପୁଫ୍",
      sortOrder: 1,
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Verified bootstrapped admin succeeds
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("subjects").doc("bse_math").set({
      boardId: "BSE_10",
      nameEn: "Mathematics",
      nameOr: "ଗଣିତ",
      sortOrder: 1,
      published: true,
      syllabusNote: "Starter syllabus",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("subjects").doc("bse_draft").set({
      boardId: "BSE_10",
      nameEn: "Draft Subject",
      nameOr: "ଡ୍ରାଫ୍ଟ",
      sortOrder: 2,
      published: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject @DocumentId 'id' inside write payload
  await assertFails(
    adminDb.collection("subjects").doc("bse_bad").set({
      id: "bse_bad",
      boardId: "BSE_10",
      nameEn: "Bad",
      nameOr: "ଖରାପ",
      sortOrder: 3,
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Student queries with published == true succeed; unfiltered list fails due to unpublished doc
  const studentDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    studentDb.collection("subjects").where("boardId", "==", "BSE_10").where("published", "==", true).get()
  );
  await assertFails(
    studentDb.collection("subjects").where("boardId", "==", "BSE_10").get()
  );
  await assertFails(studentDb.collection("subjects").doc("bse_draft").get());
  await assertFails(
    studentDb.collection("subjects").doc("bse_math").update({
      nameEn: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
