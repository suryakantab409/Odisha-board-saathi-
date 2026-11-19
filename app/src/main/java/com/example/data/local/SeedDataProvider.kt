package com.example.data.local

import com.example.model.BoardType
import com.example.model.ContentLabel
import com.example.model.Difficulty
import com.example.model.NoteType
import com.example.model.QuestionCategory

object SeedDataProvider {

    val starterSubjects = listOf(
        // BSE Odisha Class 10 (Starter Demonstration Catalog)
        SubjectEntity("bse_mil_odia", BoardType.BSE_10.id, "MIL (Odia)", "ମାତୃଭାଷା (ଓଡ଼ିଆ)", 1, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("bse_english", BoardType.BSE_10.id, "English", "ଇଂରାଜୀ", 2, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("bse_math", BoardType.BSE_10.id, "Mathematics", "ଗଣିତ", 3, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("bse_science", BoardType.BSE_10.id, "General Science", "ସାଧାରଣ ବିଜ୍ଞାନ", 4, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("bse_social", BoardType.BSE_10.id, "Social Science", "ସାମାଜିକ ବିଜ୍ଞାନ", 5, true, "Starter catalog · Editable by Admin"),

        // CHSE Odisha +2 Arts (Starter Demonstration Catalog)
        SubjectEntity("chse_odia", BoardType.CHSE_12_ARTS.id, "MIL (Odia)", "ମାତୃଭାଷା (ଓଡ଼ିଆ)", 1, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("chse_english", BoardType.CHSE_12_ARTS.id, "English", "ଇଂରାଜୀ", 2, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("chse_history", BoardType.CHSE_12_ARTS.id, "History", "ଇତିହାସ", 3, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("chse_polsci", BoardType.CHSE_12_ARTS.id, "Political Science", "ରାଜନୀତି ବିଜ୍ଞାନ", 4, true, "Starter catalog · Editable by Admin"),
        SubjectEntity("chse_economics", BoardType.CHSE_12_ARTS.id, "Economics", "ଅର୍ଥଶାସ୍ତ୍ର", 5, true, "Starter catalog · Editable by Admin")
    )

    val starterChapters = listOf(
        // BSE Class 10 Chapters
        ChapterEntity("bse_mil_ch1", BoardType.BSE_10.id, "bse_mil_odia", "Bande Utkala Janani (Demo Unit)", "ବନ୍ଦେ ଉତ୍କଳ ଜନନୀ (ନମୁନା ଅଧ୍ୟାୟ)", 1, true),
        ChapterEntity("bse_mil_ch2", BoardType.BSE_10.id, "bse_mil_odia", "Odia Grammar & Composition", "ଓଡ଼ିଆ ବ୍ୟାକରଣ ଓ ରଚନା", 2, true),
        ChapterEntity("bse_eng_ch1", BoardType.BSE_10.id, "bse_english", "Prose & Comprehension", "ଗଦ୍ୟ ଏବଂ ବୋଧଜ୍ଞାନ", 1, true),
        ChapterEntity("bse_math_ch1", BoardType.BSE_10.id, "bse_math", "Linear & Quadratic Equations", "ସରଳ ଓ ଦ୍ୱିଘାତ ସମୀକରଣ", 1, true),
        ChapterEntity("bse_math_ch2", BoardType.BSE_10.id, "bse_math", "Geometry & Triangles", "ଜ୍ୟାମିତି ଓ ତ୍ରିଭୁଜ", 2, true),
        ChapterEntity("bse_sci_ch1", BoardType.BSE_10.id, "bse_science", "Chemical Reactions & Equations", "ରାସାୟନିକ ପ୍ରତିକ୍ରିୟା ଓ ସମୀକରଣ", 1, true),
        ChapterEntity("bse_sci_ch2", BoardType.BSE_10.id, "bse_science", "Light: Reflection & Refraction", "ଆଲୋକ: ପ୍ରତିଫଳନ ଓ ପ୍ରତିସରଣ", 2, true),
        ChapterEntity("bse_soc_ch1", BoardType.BSE_10.id, "bse_social", "Freedom Movement & Odisha", "ସ୍ୱାଧୀନତା ଆନ୍ଦୋଳନ ଓ ଓଡ଼ିଶା", 1, true),

        // CHSE +2 Arts Chapters
        ChapterEntity("chse_odia_ch1", BoardType.CHSE_12_ARTS.id, "chse_odia", "Odia Prose & Literary Appreciation", "ଓଡ଼ିଆ ଗଦ୍ୟ ସାହିତ୍ୟ", 1, true),
        ChapterEntity("chse_eng_ch1", BoardType.CHSE_12_ARTS.id, "chse_english", "Essay Writing & Grammar", "ପ୍ରବନ୍ଧ ଓ ବ୍ୟାକରଣ", 1, true),
        ChapterEntity("chse_hist_ch1", BoardType.CHSE_12_ARTS.id, "chse_history", "Ancient Indian Civilization & Heritage", "ପ୍ରାଚୀନ ଭାରତୀୟ ସଭ୍ୟତା ଓ ଐତିହ୍ୟ", 1, true),
        ChapterEntity("chse_hist_ch2", BoardType.CHSE_12_ARTS.id, "chse_history", "Kalinga & Medieval Odisha", "କଳିଙ୍ଗ ଏବଂ ମଧ୍ୟଯୁଗୀୟ ଓଡ଼ିଶା", 2, true),
        ChapterEntity("chse_pol_ch1", BoardType.CHSE_12_ARTS.id, "chse_polsci", "Democracy & Indian Constitution", "ଗଣତନ୍ତ୍ର ଏବଂ ଭାରତୀୟ ସମ୍ବିଧାନ", 1, true),
        ChapterEntity("chse_eco_ch1", BoardType.CHSE_12_ARTS.id, "chse_economics", "Microeconomics: Demand & Utility", "ବ୍ୟଷ୍ଟି ଅର୍ଥଶାସ୍ତ୍ର: ଚାହିଦା ଓ ଉପଯୋଗିତା", 1, true)
    )

    val starterImportantQuestions = listOf(
        ImportantQuestionEntity(
            id = "imp_bse_1",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_math",
            chapterId = "bse_math_ch1",
            marks = 2,
            category = QuestionCategory.TWO_MARKS.name,
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            questionEn = "Write the standard form of a quadratic equation and state the discriminant formula.",
            questionOr = "ଦ୍ୱିଘାତ ସମୀକରଣର ସାଧାରଣ ରୂପ ଲେଖ ଏବଂ ନିରୂପକ (Discriminant) ସୂତ୍ର ଉଲ୍ଲେଖ କର।",
            answerEn = "Standard form: ax^2 + bx + c = 0 where a != 0. Discriminant D = b^2 - 4ac. Roots are real if D >= 0.",
            answerOr = "ସାଧାରଣ ରୂପ: ax^2 + bx + c = 0 (ଯେଉଁଠି a != 0)। ନିରୂପକ D = b^2 - 4ac। ଯଦି D >= 0 ହୁଏ ତେବେ ମୂଳ ବାସ୍ତବ ଅଟେ।",
            published = true
        ),
        ImportantQuestionEntity(
            id = "imp_bse_2",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_sci",
            chapterId = "bse_sci_ch1",
            marks = 3,
            category = QuestionCategory.THREE_MARKS.name,
            difficulty = Difficulty.MEDIUM.name,
            contentLabel = ContentLabel.ADMIN_APPROVED.name,
            sourceYear = "",
            questionEn = "Why must a chemical equation be balanced? Explain with one example.",
            questionOr = "ରାସାୟନିକ ସମୀକରଣକୁ ସନ୍ତୁଳିତ କରିବା କାହିଁକି ଆବଶ୍ୟକ? ଏକ ଉଦାହରଣ ସହ ବୁଝାଅ।",
            answerEn = "According to the Law of Conservation of Mass, matter can neither be created nor destroyed in a chemical reaction. Hence, total atoms of each element on both sides must be equal. Example: 2H2 + O2 -> 2H2O.",
            answerOr = "ବସ୍ତୁତ୍ୱ ସଂରକ୍ଷଣ ନିୟମ ଅନୁଯାୟୀ ରାସାୟନିକ ପ୍ରତିକ୍ରିୟାରେ ପଦାର୍ଥ ସୃଷ୍ଟି କିମ୍ବା ବିନାଶ ହୁଏ ନାହିଁ। ତେଣୁ ଉଭୟ ପାର୍ଶ୍ୱରେ ପ୍ରତ୍ୟେକ ମୌଳିକର ପରମାଣୁ ସଂଖ୍ୟା ସମାନ ହେବା ଉଚିତ। ଉଦାହରଣ: 2H2 + O2 -> 2H2O।",
            published = true
        ),
        ImportantQuestionEntity(
            id = "imp_bse_3",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_social",
            chapterId = "bse_soc_ch1",
            marks = 5,
            category = QuestionCategory.LONG_ANSWER.name,
            difficulty = Difficulty.MEDIUM.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            questionEn = "Discuss the major contributions of Utkalmani Gopabandhu Das to Odisha's social and national awakening.",
            questionOr = "ଓଡ଼ିଶାର ସାମାଜିକ ଓ ଜାତୀୟ ଜାଗରଣରେ ଉତ୍କଳମଣି ଗୋପବନ୍ଧୁ ଦାସଙ୍କ ଅବଦାନ ଆଲୋଚନା କର।",
            answerEn = "Gopabandhu Das founded the Satyabadi Vana Vidyalaya in 1909 to promote value-based education, started 'The Samaja' newspaper in 1919 for public awareness, and actively participated in the Non-Cooperation Movement and Odisha province formation efforts.",
            answerOr = "ଗୋପବନ୍ଧୁ ଦାସ ୧୯୦୯ ରେ ସତ୍ୟବାଦୀ ବନବିଦ୍ୟାଳୟ ପ୍ରତିଷ୍ଠା କରିଥିଲେ, ୧୯୧୯ ରେ 'ସମାଜ' ସମ୍ବାଦପତ୍ର ଆରମ୍ଭ କରି ଜନଜାଗରଣ ସୃଷ୍ଟି କରିଥିଲେ ଏବଂ ଅସହଯୋଗ ଆନ୍ଦୋଳନ ଓ ସ୍ୱତନ୍ତ୍ର ଓଡ଼ିଶା ପ୍ରଦେଶ ଗଠନରେ ପ୍ରମୁଖ ଭୂମିକା ଗ୍ରହଣ କରିଥିଲେ।",
            published = true
        ),
        ImportantQuestionEntity(
            id = "imp_bse_4",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_mil_odia",
            chapterId = "bse_mil_ch1",
            marks = 1,
            category = QuestionCategory.ONE_MARK.name,
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.ADMIN_APPROVED.name,
            sourceYear = "",
            questionEn = "Who composed Odisha's state anthem 'Bande Utkala Janani'?",
            questionOr = "ଓଡ଼ିଶାର ରାଜ୍ୟ ସଙ୍ଗୀତ 'ବନ୍ଦେ ଉତ୍କଳ ଜନନୀ' କିଏ ରଚନା କରିଥିଲେ?",
            answerEn = "Kantakabi Laxmikanta Mohapatra composed 'Bande Utkala Janani'.",
            answerOr = "କାନ୍ତକବି ଲକ୍ଷ୍ମୀକାନ୍ତ ମହାପାତ୍ର 'ବନ୍ଦେ ଉତ୍କଳ ଜନନୀ' ରଚନା କରିଥିଲେ।",
            published = true
        ),
        // CHSE +2 Arts Important Questions
        ImportantQuestionEntity(
            id = "imp_chse_1",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_history",
            chapterId = "chse_hist_ch2",
            marks = 5,
            category = QuestionCategory.LONG_ANSWER.name,
            difficulty = Difficulty.MEDIUM.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            questionEn = "Explain the causes and far-reaching significance of the Kalinga War of 261 BCE.",
            questionOr = "ଖ୍ରୀଷ୍ଟପୂର୍ବ ୨୬୧ ର କଳିଙ୍ଗ ଯୁଦ୍ଧର କାରଣ ଓ ସୁଦୂରପ୍ରସାରୀ ପ୍ରଭାବ ବର୍ଣ୍ଣନା କର।",
            answerEn = "Emperor Ashoka invaded Kalinga for political unification and maritime trade access. The immense bloodshed transformed Ashoka from Chandashoka to Dharmashoka, leading him to embrace Dhamma (peace and non-violence) and propagate Buddhism across Asia.",
            answerOr = "ରାଜନୈତିକ ଏକତା ଓ ସାମୁଦ୍ରିକ ବାଣିଜ୍ୟ ନିୟନ୍ତ୍ରଣ ପାଇଁ ସମ୍ରାଟ ଅଶୋକ କଳିଙ୍ଗ ଆକ୍ରମଣ କରିଥିଲେ। ଭୟଙ୍କର ରକ୍ତପାତ ଅଶୋକଙ୍କ ହୃଦୟ ପରିବର୍ତ୍ତନ କଲା ଏବଂ ସେ ଧମ୍ମ ଓ ଅହିଂସା ମାର୍ଗ ଗ୍ରହଣ କରି ବୌଦ୍ଧ ଧର୍ମ ପ୍ରଚାର କଲେ।",
            published = true
        ),
        ImportantQuestionEntity(
            id = "imp_chse_2",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_polsci",
            chapterId = "chse_pol_ch1",
            marks = 3,
            category = QuestionCategory.VERY_IMPORTANT.name,
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.ADMIN_APPROVED.name,
            sourceYear = "",
            questionEn = "Distinguish between Direct Democracy and Representative (Indirect) Democracy.",
            questionOr = "ପ୍ରତ୍ୟକ୍ଷ ଗଣତନ୍ତ୍ର ଏବଂ ପରୋକ୍ଷ (ପ୍ରତିନିଧିମୂଳକ) ଗଣତନ୍ତ୍ର ମଧ୍ୟରେ ପାର୍ଥକ୍ୟ ଲେଖ।",
            answerEn = "In Direct Democracy, citizens participate directly in lawmaking (e.g., referendum). In Representative Democracy, citizens elect representatives who govern on their behalf (e.g., India's parliamentary system).",
            answerOr = "ପ୍ରତ୍ୟକ୍ଷ ଗଣତନ୍ତ୍ରରେ ନାଗରିକମାନେ ସିଧାସଳଖ ଆଇନ ପ୍ରଣୟନରେ ଅଂଶଗ୍ରହଣ କରନ୍ତି। ପରୋକ୍ଷ ଗଣତନ୍ତ୍ରରେ ନାଗରିକମାନେ ଭୋଟ୍ ଦେଇ ପ୍ରତିନିଧି ନିର୍ବାଚିତ କରନ୍ତି ଯେଉଁମାନେ ଶାସନ ପରିଚାଳନା କରନ୍ତି (ଯଥା ଭାରତ)।",
            published = true
        ),
        ImportantQuestionEntity(
            id = "imp_chse_3",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_economics",
            chapterId = "chse_eco_ch1",
            marks = 2,
            category = QuestionCategory.TWO_MARKS.name,
            difficulty = Difficulty.MEDIUM.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            questionEn = "State the Law of Diminishing Marginal Utility.",
            questionOr = "କ୍ରମହ୍ରାସମାନ ସୀମାନ୍ତ ଉପଯୋଗିତା ନିୟମ ଲେଖ।",
            answerEn = "Other things remaining constant, as a consumer consumes successive units of a commodity, the additional (marginal) utility derived from each extra unit tends to decline.",
            answerOr = "ଅନ୍ୟ ସମସ୍ତ ଅବସ୍ଥା ସ୍ଥିର ରହିଲେ, ଜଣେ ଉପଭୋକ୍ତା କୌଣସି ଦ୍ରବ୍ୟର ଅଧିକରୁ ଅଧିକ ଏକକ ବ୍ୟବହାର କଲେ ପ୍ରତ୍ୟେକ ଅତିରିକ୍ତ ଏକକରୁ ମିଳୁଥିବା ସୀମାନ୍ତ ଉପଯୋଗିତା କ୍ରମଶଃ ହ୍ରାସ ପାଏ।",
            published = true
        )
    )

    val starterMcqs = listOf(
        // BSE Class 10 MCQs
        McqEntity(
            id = "mcq_bse_1",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_mil_odia",
            chapterId = "bse_mil_ch1",
            questionEn = "Who is the author of 'Bande Utkala Janani'?",
            questionOr = "'ବନ୍ଦେ ଉତ୍କଳ ଜନନୀ'ର ରଚୟିତା କିଏ?",
            optionsEn = listOf("Fakir Mohan Senapati", "Kantakabi Laxmikanta Mohapatra", "Gopabandhu Das", "Gangadhar Meher"),
            optionsOr = listOf("ଫକୀର ମୋହନ ସେନାପତି", "କାନ୍ତକବି ଲକ୍ଷ୍ମୀକାନ୍ତ ମହାପାତ୍ର", "ଗୋପବନ୍ଧୁ ଦାସ", "ଗଙ୍ଗାଧର ମେହେର"),
            correctIndex = 1,
            explanationEn = "Kantakabi Laxmikanta Mohapatra wrote 'Bande Utkala Janani' in 1912.",
            explanationOr = "କାନ୍ତକବି ଲକ୍ଷ୍ମୀକାନ୍ତ ମହାପାତ୍ର ୧୯୧୨ ମସିହାରେ 'ବନ୍ଦେ ଉତ୍କଳ ଜନନୀ' ରଚନା କରିଥିଲେ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_bse_2",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_math",
            chapterId = "bse_math_ch1",
            questionEn = "If the discriminant b^2 - 4ac = 0 of a quadratic equation, then the roots are:",
            questionOr = "ଯଦି ଏକ ଦ୍ୱିଘାତ ସମୀକରଣର ନିରୂପକ b^2 - 4ac = 0 ହୁଏ, ତେବେ ମୂଳଗୁଡ଼ିକ କିପରି ହେବ?",
            optionsEn = listOf("Real and unequal", "Real and equal", "Imaginary", "Zero only"),
            optionsOr = listOf("ବାସ୍ତବ ଓ ଅସମାନ", "ବାସ୍ତବ ଓ ସମାନ", "କାଳ୍ପନିକ", "କେବଳ ଶୂନ"),
            correctIndex = 1,
            explanationEn = "When D = b^2 - 4ac = 0, both roots equal -b / (2a), so they are real and equal.",
            explanationOr = "ଯେତେବେଳେ D = 0 ହୁଏ, ଉଭୟ ମୂଳ -b / (2a) ସହ ସମାନ ହୁଏ, ଅର୍ଥାତ୍ ବାସ୍ତବ ଓ ସମାନ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_bse_3",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_math",
            chapterId = "bse_math_ch2",
            questionEn = "The sum of all interior angles of a triangle is always:",
            questionOr = "ଏକ ତ୍ରିଭୁଜର ତିନୋଟି ଅନ୍ତଃସ୍ଥ କୋଣର ସମଷ୍ଟି ସର୍ବଦା କେତେ?",
            optionsEn = listOf("90 degrees", "180 degrees", "270 degrees", "360 degrees"),
            optionsOr = listOf("୯୦ ଡିଗ୍ରୀ", "୧୮୦ ଡିଗ୍ରୀ", "୨୭୦ ଡିଗ୍ରୀ", "୩୬୦ ଡିଗ୍ରୀ"),
            correctIndex = 1,
            explanationEn = "By the angle sum property of triangles in Euclidean geometry, interior angles sum to 180 degrees.",
            explanationOr = "ତ୍ରିଭୁଜର କୋଣ ସମଷ୍ଟି ନିୟମ ଅନୁଯାୟୀ ତିନୋଟି ଅନ୍ତଃସ୍ଥ କୋଣର ସମଷ୍ଟି ୧୮୦ ଡିଗ୍ରୀ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_bse_4",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_science",
            chapterId = "bse_sci_ch1",
            questionEn = "Which gas is produced when zinc granules react with dilute hydrochloric acid?",
            questionOr = "ଜିଙ୍କ୍ ଦାନା ସହିତ ଲଘୁ ହାଇଡ୍ରୋକ୍ଲୋରିକ୍ ଏସିଡ୍‌ର ପ୍ରତିକ୍ରିୟାରେ କେଉଁ ଗ୍ୟାସ୍ ନିର୍ଗତ ହୁଏ?",
            optionsEn = listOf("Oxygen", "Hydrogen", "Nitrogen", "Carbon dioxide"),
            optionsOr = listOf("ଅମ୍ଳଜାନ", "ଉଦ୍‌ଜାନ (Hydrogen)", "ଯବକ୍ଷାରଜାନ", "ଅଙ୍ଗାରକାମ୍ଳ"),
            correctIndex = 1,
            explanationEn = "Zn + 2HCl -> ZnCl2 + H2 (Hydrogen gas).",
            explanationOr = "Zn + 2HCl -> ZnCl2 + H2 (ଉଦ୍‌ଜାନ ଗ୍ୟାସ୍ ନିର୍ଗତ ହୁଏ)।",
            difficulty = Difficulty.MEDIUM.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_bse_5",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_science",
            chapterId = "bse_sci_ch2",
            questionEn = "The SI unit of power of a lens is:",
            questionOr = "ଲେନ୍ସର କ୍ଷମତାର SI ଏକକ କ'ଣ?",
            optionsEn = listOf("Watt", "Joule", "Dioptre", "Newton"),
            optionsOr = listOf("ୱାଟ୍", "ଜୁଲ୍", "ଡାୟୋପ୍ଟର", "ନିଉଟନ୍"),
            correctIndex = 2,
            explanationEn = "Power P = 1 / f (in metres), measured in Dioptres (D).",
            explanationOr = "ଲେନ୍ସର କ୍ଷମତା P = 1 / f (ମିଟରରେ), ଯାହାର ଏକକ ଡାୟୋପ୍ଟର (D)।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_bse_6",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_social",
            chapterId = "bse_soc_ch1",
            questionEn = "In which year did Odisha become a separate state on a linguistic basis?",
            questionOr = "ଭାଷା ଭିତ୍ତିରେ ଓଡ଼ିଶା କେଉଁ ବର୍ଷ ଏକ ସ୍ୱତନ୍ତ୍ର ପ୍ରଦେଶ ଭାବେ ଗଠିତ ହୋଇଥିଲା?",
            optionsEn = listOf("1930", "1936", "1947", "1950"),
            optionsOr = listOf("୧୯୩୦", "୧୯୩୬", "୧୯୪୭", "୧୯୫୦"),
            correctIndex = 1,
            explanationEn = "Odisha became a separate province on 1 April 1936 (Utkal Dibasa).",
            explanationOr = "୧ ଏପ୍ରିଲ୍ ୧୯୩୬ ମସିହାରେ ଓଡ଼ିଶା ସ୍ୱତନ୍ତ୍ର ପ୍ରଦେଶ ଭାବେ ଗଠିତ ହୋଇଥିଲା।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.ADMIN_APPROVED.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_bse_7",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_english",
            chapterId = "bse_eng_ch1",
            questionEn = "Choose the grammatically correct passive voice of: 'She writes a letter.'",
            questionOr = "'She writes a letter.' ବାକ୍ୟର ସଠିକ୍ Passive Voice ବାଛ:",
            optionsEn = listOf("A letter was written by her.", "A letter is written by her.", "A letter is writing by her.", "A letter has written by her."),
            optionsOr = listOf("A letter was written by her.", "A letter is written by her.", "A letter is writing by her.", "A letter has written by her."),
            correctIndex = 1,
            explanationEn = "Simple present active 'writes' transforms to 'is written' in passive voice.",
            explanationOr = "Simple present active 'writes' ର passive ରୂପ 'is written' ଅଟେ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),

        // CHSE +2 Arts MCQs
        McqEntity(
            id = "mcq_chse_1",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_history",
            chapterId = "chse_hist_ch2",
            questionEn = "The famous Kalinga War was fought in which year BCE?",
            questionOr = "ପ୍ରସିଦ୍ଧ କଳିଙ୍ଗ ଯୁଦ୍ଧ କେଉଁ ଖ୍ରୀଷ୍ଟପୂର୍ବ ବର୍ଷରେ ହୋଇଥିଲା?",
            optionsEn = listOf("326 BCE", "261 BCE", "185 BCE", "78 CE"),
            optionsOr = listOf("୩୨୬ ଖ୍ରୀ.ପୂ.", "୨୬୧ ଖ୍ରୀ.ପୂ.", "୧୮୫ ଖ୍ରୀ.ପୂ.", "୭୮ ଖ୍ରୀ.ଅ."),
            correctIndex = 1,
            explanationEn = "Emperor Ashoka fought the Kalinga War in 261 BCE, recorded in Rock Edict XIII.",
            explanationOr = "ସମ୍ରାଟ ଅଶୋକ ଖ୍ରୀଷ୍ଟପୂର୍ବ ୨୬୧ ରେ କଳିଙ୍ଗ ଯୁଦ୍ଧ କରିଥିଲେ, ଯାହା ଶିଳାଲେଖ XIII ରେ ଉଲ୍ଲେଖ ଅଛି।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_chse_2",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_history",
            chapterId = "chse_hist_ch1",
            questionEn = "Which Indus Valley site is famous for its dockyard?",
            questionOr = "ସିନ୍ଧୁ ସଭ୍ୟତାର କେଉଁ ସ୍ଥାନ ବନ୍ଦର (Dockyard) ପାଇଁ ପ୍ରସିଦ୍ଧ?",
            optionsEn = listOf("Harappa", "Mohenjo-daro", "Lothal", "Kalibangan"),
            optionsOr = listOf("ହରପ୍ପା", "ମହେଞ୍ଜୋଦାରୋ", "ଲୋଥାଲ୍", "କାଲିବଙ୍ଗାନ୍"),
            correctIndex = 2,
            explanationEn = "Lothal in Gujarat had a brick-built tidal dockyard used for maritime trade.",
            explanationOr = "ଗୁଜୁରାଟର ଲୋଥାଲ୍ ପ୍ରାଚୀନ ସାମୁଦ୍ରିକ ବନ୍ଦର ପାଇଁ ପ୍ରସିଦ୍ଧ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_chse_3",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_polsci",
            chapterId = "chse_pol_ch1",
            questionEn = "Which Article of the Indian Constitution deals with the Right to Equality before Law?",
            questionOr = "ଭାରତୀୟ ସମ୍ବିଧାନର କେଉଁ ଅନୁଚ୍ଛେଦ ଆଇନ ଆଗରେ ସମାନତା ଅଧିକାର ସହ ସମ୍ପର୍କିତ?",
            optionsEn = listOf("Article 14", "Article 19", "Article 21", "Article 32"),
            optionsOr = listOf("ଅନୁଚ୍ଛେଦ ୧୪", "ଅନୁଚ୍ଛେଦ ୧୯", "ଅନୁଚ୍ଛେଦ ୨୧", "ଅନୁଚ୍ଛେଦ ୩୨"),
            correctIndex = 0,
            explanationEn = "Article 14 guarantees equality before the law and equal protection of the laws.",
            explanationOr = "ଅନୁଚ୍ଛେଦ ୧୪ ଆଇନ ଆଗରେ ସମାନତା ଏବଂ ଆଇନର ସମାନ ସୁରକ୍ଷା ପ୍ରଦାନ କରେ।",
            difficulty = Difficulty.MEDIUM.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_chse_4",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_economics",
            chapterId = "chse_eco_ch1",
            questionEn = "According to the Law of Demand, when price of a normal good rises (ceteris paribus), quantity demanded:",
            questionOr = "ଚାହିଦା ନିୟମ ଅନୁଯାୟୀ କୌଣସି ସାଧାରଣ ଦ୍ରବ୍ୟର ଦର ବଢ଼ିଲେ ଚାହିଦାର ପରିମାଣ:",
            optionsEn = listOf("Increases", "Decreases", "Remains infinite", "Becomes zero"),
            optionsOr = listOf("ବଢ଼େ", "କମେ", "ଅସୀମ ରହେ", "ଶୂନ ହୁଏ"),
            correctIndex = 1,
            explanationEn = "There is an inverse relationship between price and quantity demanded for normal goods.",
            explanationOr = "ସାଧାରଣ ଦ୍ରବ୍ୟ ପାଇଁ ଦର ଏବଂ ଚାହିଦା ପରିମାଣ ମଧ୍ୟରେ ବିପରୀତ ସମ୍ପର୍କ ଥାଏ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        ),
        McqEntity(
            id = "mcq_chse_5",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_english",
            chapterId = "chse_eng_ch1",
            questionEn = "Identify the correctly punctuated formal salutation in an official letter:",
            questionOr = "ଏକ ସରକାରୀ ପତ୍ରରେ ସଠିକ୍ ସମ୍ବୋଧନ ଚିହ୍ନଟ କର:",
            optionsEn = listOf("Dear Sir/Madam,", "hi sir!", "Hey there", "To whom it"),
            optionsOr = listOf("Dear Sir/Madam,", "hi sir!", "Hey there", "To whom it"),
            correctIndex = 0,
            explanationEn = "'Dear Sir/Madam,' is the standard formal salutation in official correspondence.",
            explanationOr = "ସରକାରୀ ପତ୍ରରେ 'Dear Sir/Madam,' ସଠିକ୍ ଆନୁଷ୍ଠାନିକ ସମ୍ବୋଧନ ଅଟେ।",
            difficulty = Difficulty.EASY.name,
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            sourceYear = "",
            published = true
        )
    )

    val starterMockExams = listOf(
        MockExamEntity(
            id = "mock_bse_demo",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_math",
            titleEn = "BSE Class 10 Starter Practice Mock (Demo Pattern)",
            titleOr = "ବିଏସ୍ଇ ଦଶମ ନମୁନା ଅଭ୍ୟାସ ମକ୍ ଟେଷ୍ଟ",
            durationMinutes = 15,
            totalMarks = 7,
            questionIds = listOf("mcq_bse_1", "mcq_bse_2", "mcq_bse_3", "mcq_bse_4", "mcq_bse_5", "mcq_bse_6", "mcq_bse_7"),
            patternNote = "Demonstration pattern only — Admin can configure official exam duration, marks, and question paper distribution.",
            published = true
        ),
        MockExamEntity(
            id = "mock_chse_demo",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_history",
            titleEn = "CHSE +2 Arts Starter Practice Mock (Demo Pattern)",
            titleOr = "ସିଏଚ୍ଏସ୍ଇ +୨ କଳା ନମୁନା ଅଭ୍ୟାସ ମକ୍ ଟେଷ୍ଟ",
            durationMinutes = 15,
            totalMarks = 5,
            questionIds = listOf("mcq_chse_1", "mcq_chse_2", "mcq_chse_3", "mcq_chse_4", "mcq_chse_5"),
            patternNote = "Demonstration pattern only — Admin can configure official CHSE +2 Arts exam pattern.",
            published = true
        )
    )

    val starterPyqs = listOf(
        PyqPaperEntity(
            id = "pyq_bse_demo",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_math",
            examYear = "Demo Template",
            questionType = "Sample Practice Set",
            titleEn = "BSE Class 10 Sample Question Paper Structure (Admin Upload Ready)",
            titleOr = "ବିଏସ୍ଇ ଦଶମ ନମୁନା ପ୍ରଶ୍ନପତ୍ର (ଆଡମିନ୍ ଅପଲୋଡ୍ ପାଇଁ ପ୍ରସ୍ତୁତ)",
            searchableText = "Starter demonstration PYQ record. Admins can upload verified BSE Odisha previous-year PDFs and searchable text here without fabricating past paper years.",
            pdfUri = "",
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            published = true
        ),
        PyqPaperEntity(
            id = "pyq_chse_demo",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_history",
            examYear = "Demo Template",
            questionType = "Sample Practice Set",
            titleEn = "CHSE +2 Arts History Sample Question Structure (Admin Upload Ready)",
            titleOr = "ସିଏଚ୍ଏସ୍ଇ +୨ କଳା ଇତିହାସ ନମୁନା ପ୍ରଶ୍ନପତ୍ର",
            searchableText = "Starter demonstration PYQ record for CHSE +2 Arts. Admins can upload verified past board papers and searchable text from the Admin Panel.",
            pdfUri = "",
            contentLabel = ContentLabel.PRACTICE_QUESTION.name,
            published = true
        )
    )

    val starterNotes = listOf(
        StudyNoteEntity(
            id = "note_bse_1",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_math",
            chapterId = "bse_math_ch1",
            noteType = NoteType.REVISION.name,
            titleEn = "Quadratic Equations Quick Revision Formula Sheet",
            titleOr = "ଦ୍ୱିଘାତ ସମୀକରଣ ତ୍ୱରିତ ସୂତ୍ର ନୋଟ୍ସ",
            contentEn = "1. Standard Form: ax^2 + bx + c = 0 (a != 0)\n2. Discriminant: D = b^2 - 4ac\n3. Quadratic Formula: x = (-b +/- sqrt(D)) / 2a\n4. Nature of Roots:\n   • D > 0: Two distinct real roots\n   • D = 0: Two equal real roots\n   • D < 0: No real roots",
            contentOr = "୧. ସାଧାରଣ ରୂପ: ax^2 + bx + c = 0 (a != 0)\n୨. ନିରୂପକ: D = b^2 - 4ac\n୩. ଶ୍ରୀଧର ଆଚାର୍ଯ୍ୟ ସୂତ୍ର: x = (-b +/- sqrt(D)) / 2a\n୪. ମୂଳର ପ୍ରକୃତି:\n   • D > 0: ଦୁଇଟି ଭିନ୍ନ ବାସ୍ତବ ମୂଳ\n   • D = 0: ଦୁଇଟି ସମାନ ବାସ୍ତବ ମୂଳ\n   • D < 0: କୌଣସି ବାସ୍ତବ ମୂଳ ନାହିଁ",
            pdfUri = "",
            published = true,
            savedOffline = true
        ),
        StudyNoteEntity(
            id = "note_bse_2",
            boardId = BoardType.BSE_10.id,
            subjectId = "bse_science",
            chapterId = "bse_sci_ch1",
            noteType = NoteType.DEFINITIONS.name,
            titleEn = "Types of Chemical Reactions — Key Definitions",
            titleOr = "ରାସାୟନିକ ପ୍ରତିକ୍ରିୟାର ପ୍ରକାର — ମୁଖ୍ୟ ସଂଜ୍ଞା",
            contentEn = "• Combination Reaction: Two or more reactants combine to form a single product (CaO + H2O -> Ca(OH)2).\n• Decomposition Reaction: A single compound breaks down into simpler substances.\n• Displacement Reaction: A more reactive element displaces a less reactive element from its compound.\n• Oxidation & Reduction (Redox): Gain of oxygen / loss of hydrogen is oxidation; reverse is reduction.",
            contentOr = "• ସଂଶ୍ଳେଷଣ ପ୍ରତିକ୍ରିୟା: ଦୁଇ ବା ତତୋଧିକ ପ୍ରତିକାରକ ମିଶି ଏକକ ଉତ୍ପାଦ ଗଠନ କରନ୍ତି।\n• ବିଘଟନ ପ୍ରତିକ୍ରିୟା: ଏକ ଯୌଗିକ ଭାଙ୍ଗି ସରଳ ପଦାର୍ଥରେ ପରିଣତ ହୁଏ।\n• ବିସ୍ଥାପନ ପ୍ରତିକ୍ରିୟା: ଅଧିକ ପ୍ରତିକ୍ରିୟାଶୀଳ ମୌଳିକ କମ୍ ପ୍ରତିକ୍ରିୟାଶୀଳ ମୌଳିକକୁ ବିସ୍ଥାପିତ କରେ।",
            pdfUri = "",
            published = true,
            savedOffline = false
        ),
        StudyNoteEntity(
            id = "note_chse_1",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_history",
            chapterId = "chse_hist_ch2",
            noteType = NoteType.CHAPTER_NOTES.name,
            titleEn = "Kalinga War & Ashoka's Dhamma — Summary Notes",
            titleOr = "କଳିଙ୍ଗ ଯୁଦ୍ଧ ଏବଂ ଅଶୋକଙ୍କ ଧମ୍ମ — ସାରାଂଶ ନୋଟ୍ସ",
            contentEn = "• Year: 261 BCE (8th regnal year of Ashoka)\n• Source: Major Rock Edict XIII\n• Impact: Transformation from military conquest (Bherighosha) to righteous conquest (Dhammaghosha).\n• Core tenets of Dhamma: Respect for elders, kindness to all living beings, religious tolerance, and welfare administration.",
            contentOr = "• ସମୟ: ଖ୍ରୀଷ୍ଟପୂର୍ବ ୨୬୧ (ଅଶୋକଙ୍କ ରାଜତ୍ୱର ଅଷ୍ଟମ ବର୍ଷ)\n• ଉତ୍ସ: ତ୍ରୟୋଦଶ ଶିଳାଲେଖ (Rock Edict XIII)\n• ପ୍ରଭାବ: ଭେରୀଘୋଷରୁ ଧମ୍ମଘୋଷକୁ ପରିବର୍ତ୍ତନ।\n• ମୁଖ୍ୟ ନୀତି: ଗୁରୁଜନଙ୍କ ପ୍ରତି ସମ୍ମାନ, ଜୀବଦୟା, ଧାର୍ମିକ ସହିଷ୍ଣୁତା ଏବଂ ଜନକଲ୍ୟାଣକାରୀ ଶାସନ।",
            pdfUri = "",
            published = true,
            savedOffline = true
        ),
        StudyNoteEntity(
            id = "note_chse_2",
            boardId = BoardType.CHSE_12_ARTS.id,
            subjectId = "chse_polsci",
            chapterId = "chse_pol_ch1",
            noteType = NoteType.SHORT_ANSWERS.name,
            titleEn = "Salient Features of the Indian Constitution",
            titleOr = "ଭାରତୀୟ ସମ୍ବିଧାନର ମୁଖ୍ୟ ବୈଶିଷ୍ଟ୍ୟ",
            contentEn = "1. Lengthiest written constitution.\n2. Parliamentary form of government.\n3. Blend of rigidity and flexibility.\n4. Fundamental Rights (Part III) & Directive Principles (Part IV).\n5. Independent Judiciary and Single Citizenship.",
            contentOr = "୧. ବିଶ୍ୱର ଦୀର୍ଘତମ ଲିଖିତ ସମ୍ବିଧାନ।\n୨. ସଂସଦୀୟ ଶାସନ ବ୍ୟବସ୍ଥା।\n୩. କଠୋରତା ଓ ନମନୀୟତାର ସମନ୍ୱୟ।\n୪. ମୌଳିକ ଅଧିକାର ଓ ରାଷ୍ଟ୍ର ନୀତି ନିର୍ଦ୍ଦେଶକ ତତ୍ତ୍ୱ।\n୫. ସ୍ୱାଧୀନ ନ୍ୟାୟପାଳିକା ଓ ଏକକ ନାଗରିକତା।",
            pdfUri = "",
            published = true,
            savedOffline = false
        )
    )
}
