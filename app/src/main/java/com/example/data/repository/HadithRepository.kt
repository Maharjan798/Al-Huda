package com.example.data.repository

import com.example.data.api.HadithApiService
import com.example.data.models.HadithUiModel
import java.util.Calendar

class HadithRepository(
    private val hadithApiService: HadithApiService
) {

    private val curatedHadiths = listOf(
        HadithUiModel(
            id = "curated_1",
            arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            englishText = "The Messenger of Allah (ﷺ) said: 'The best among you are those who learn the Qur'an and teach it.'",
            narrator = "Narrated Uthman bin Affan",
            bookName = "Sahih al-Bukhari",
            refNo = "Hadith 5027",
            chapterName = "Virtues of the Quran",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_2",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            englishText = "The Prophet (ﷺ) said: 'Actions are judged by intentions, and every person will get what was intended.'",
            narrator = "Narrated Umar bin Al-Khattab",
            bookName = "Sahih al-Bukhari & Muslim",
            refNo = "Hadith 1",
            chapterName = "Book of Faith",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_3",
            arabicText = "خِيَارُكُمْ أَحَاسِنُكُمْ أَخْلاَقًا",
            englishText = "The Messenger of Allah (ﷺ) said: 'The best amongst you are those who have the best manners and character.'",
            narrator = "Narrated Abdullah ibn Amr",
            bookName = "Sahih al-Bukhari",
            refNo = "Hadith 3559",
            chapterName = "Book of Virtues",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_4",
            arabicText = "الدِّينُ النَّصِيحَةُ",
            englishText = "The Prophet (ﷺ) said: 'Religion is sincerity and good counsel.'",
            narrator = "Narrated Tamim ad-Dari",
            bookName = "Sahih Muslim",
            refNo = "Hadith 55",
            chapterName = "Book of Faith",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_5",
            arabicText = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            englishText = "The Messenger of Allah (ﷺ) said: 'None of you truly believes until he loves for his brother what he loves for himself.'",
            narrator = "Narrated Anas bin Malik",
            bookName = "Sahih al-Bukhari",
            refNo = "Hadith 13",
            chapterName = "Book of Faith",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_6",
            arabicText = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            englishText = "The Messenger of Allah (ﷺ) said: 'Whoever believes in Allah and the Last Day should speak good or remain silent.'",
            narrator = "Narrated Abu Hurairah",
            bookName = "Sahih al-Bukhari",
            refNo = "Hadith 6018",
            chapterName = "Good Manners",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_7",
            arabicText = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
            englishText = "The Prophet (ﷺ) said: 'A Muslim is the one from whose tongue and hand other Muslims are safe.'",
            narrator = "Narrated Abdullah bin Amr",
            bookName = "Sahih al-Bukhari",
            refNo = "Hadith 10",
            chapterName = "Book of Faith",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_8",
            arabicText = "احْفَظِ اللَّهَ يَحْفَظْكَ، احْفَظِ اللَّهَ تَجِدْهُ تُجَاهَكَ",
            englishText = "The Messenger of Allah (ﷺ) said: 'Be mindful of Allah and Allah will protect you. Be mindful of Allah and you will find Him before you.'",
            narrator = "Narrated Abdullah ibn Abbas",
            bookName = "Jami` at-Tirmidhi",
            refNo = "Hadith 2516",
            chapterName = "An-Nawawi Forty Hadith #19",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_9",
            arabicText = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
            englishText = "The Prophet (ﷺ) said: 'Your smiling in the face of your brother is charity for you.'",
            narrator = "Narrated Abu Dharr",
            bookName = "Jami` at-Tirmidhi",
            refNo = "Hadith 1956",
            chapterName = "Book of Righteousness",
            grade = "Sahih"
        ),
        HadithUiModel(
            id = "curated_10",
            arabicText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
            englishText = "The Messenger of Allah (ﷺ) said: 'The merciful will be shown mercy by the Most Merciful. Be merciful to those on the earth and the One above the heavens will have mercy upon you.'",
            narrator = "Narrated Abdullah bin Amr",
            bookName = "Jami` at-Tirmidhi",
            refNo = "Hadith 1924",
            chapterName = "Book of Mercy",
            grade = "Sahih"
        )
    )

    suspend fun getHadithOfTheDay(forceRandom: Boolean = false): HadithUiModel {
        if (!forceRandom) {
            // Pick deterministic curated for offline/daily consistency first as default
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val fallbackIndex = dayOfYear % curatedHadiths.size
            val defaultHadith = curatedHadiths[fallbackIndex]

            try {
                val books = listOf("bukhari", "muslim", "tirmidhi", "abudawud")
                val selectedBook = books[dayOfYear % books.size]
                val response = hadithApiService.getRandomHadith(selectedBook)
                val dto = response.data

                val englishText = dto?.hadithEnglish?.trim()
                if (dto != null && !englishText.isNullOrEmpty()) {
                    return HadithUiModel(
                        id = "api_${dto.refno ?: System.currentTimeMillis()}",
                        arabicText = dto.hadithArabic?.trim()?.takeIf { it.isNotBlank() } ?: defaultHadith.arabicText,
                        englishText = englishText,
                        narrator = dto.header?.takeIf { it.isNotBlank() } ?: defaultHadith.narrator,
                        bookName = dto.bookName?.takeIf { it.isNotBlank() } ?: "Sahih ${selectedBook.replaceFirstChar { it.uppercase() }}",
                        refNo = dto.refno?.takeIf { it.isNotBlank() } ?: "Daily Hadith",
                        chapterName = dto.headerArabic,
                        grade = "Sahih"
                    )
                }
            } catch (e: Exception) {
                // Return default daily offline fallback smoothly
            }
            return defaultHadith
        } else {
            // Force random API call or pick random curated
            val books = listOf("bukhari", "muslim", "tirmidhi", "abudawud", "nasai").shuffled()
            for (book in books) {
                try {
                    val response = hadithApiService.getRandomHadith(book)
                    val dto = response.data
                    val englishText = dto?.hadithEnglish?.trim()
                    if (dto != null && !englishText.isNullOrEmpty()) {
                        return HadithUiModel(
                            id = "api_rand_${System.currentTimeMillis()}",
                            arabicText = dto.hadithArabic?.trim()?.takeIf { it.isNotBlank() } ?: curatedHadiths.random().arabicText,
                            englishText = englishText,
                            narrator = dto.header?.takeIf { it.isNotBlank() } ?: "Narrated Companion",
                            bookName = dto.bookName?.takeIf { it.isNotBlank() } ?: "Sahih ${book.replaceFirstChar { it.uppercase() }}",
                            refNo = dto.refno?.takeIf { it.isNotBlank() } ?: "Hadith Inspiration",
                            chapterName = dto.headerArabic,
                            grade = "Sahih"
                        )
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            return curatedHadiths.random()
        }
    }

    private fun String?.isNull_or_Blank(): Boolean = this == null || this.trim().isEmpty()
    private fun String.capitalize(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
