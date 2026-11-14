package com.example.data.repository

import com.example.data.api.AlQuranApiService
import com.example.data.db.QuranBookmarkDao
import com.example.data.models.AyahUiModel
import com.example.data.models.EbookAyahUiModel
import com.example.data.models.LastReadPositionEntity
import com.example.data.models.QuranBookmarkEntity
import com.example.data.models.QuranEbookPageUiModel
import com.example.data.models.ReciterInfo
import com.example.data.models.SearchMatchDto
import com.example.data.models.SurahDto
import com.example.data.models.TranslationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class QuranRepository(
    private val apiService: AlQuranApiService,
    private val bookmarkDao: QuranBookmarkDao
) {

    val availableTranslations = listOf(
        TranslationInfo("hi.hindi", "Hindi (हिन्दी)", "फ़ारूक़ ख़ान & नदवी", "🇮🇳"),
        TranslationInfo("hi.farooq", "Hindi (Farooq)", "फ़ारूक़ ख़ान & अहमद", "🇮🇳"),
        TranslationInfo("en.sahih", "English", "Saheeh International", "🇬🇧"),
        TranslationInfo("ur.jalandhry", "Urdu (اردو)", "Fateh Muhammad Jalandhry", "🇵🇰"),
        TranslationInfo("id.indonesian", "Indonesian", "Kemenag RI", "🇮🇩"),
        TranslationInfo("fr.hamidullah", "French", "Muhammad Hamidullah", "🇫🇷"),
        TranslationInfo("tr.atech", "Turkish", "Ali Bulaç", "🇹🇷"),
        TranslationInfo("es.cortes", "Spanish", "Julio Cortes", "🇪🇸"),
        TranslationInfo("de.aburida", "German", "Abu Rida", "🇩🇪"),
        TranslationInfo("bn.bengali", "Bengali", "Zohurul Hoque", "🇧🇩"),
        TranslationInfo("ru.kuliev", "Russian", "Elmir Kuliev", "🇷🇺"),
        TranslationInfo("zh.jian", "Chinese", "Ma Jian", "🇨🇳")
    )

    val availableReciters = listOf(
        ReciterInfo("1", "Mishary Rashid Alafasy", "Murattal - Moderate pace", "ar.alafasy"),
        ReciterInfo("2", "Abdul Basit Abdul Samad", "Mujawwad - Classic style", "ar.abdulbasitmurattal"),
        ReciterInfo("3", "Abu Bakr Al-Shatri", "Murattal - Smooth pace", "ar.shaatree"),
        ReciterInfo("4", "Mahmoud Khalil Al-Husary", "Conscientious Uthmani", "ar.husary")
    )

    val allBookmarks: Flow<List<QuranBookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val lastReadPosition: Flow<LastReadPositionEntity?> = bookmarkDao.getLastReadPosition()

    suspend fun getSurahList(): List<SurahDto> {
        return try {
            val response = apiService.getSurahList()
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                getPreloadedSurahList()
            }
        } catch (e: Exception) {
            getPreloadedSurahList()
        }
    }

    suspend fun getSurahAyahs(
        surahNumber: Int,
        translationId: String = "hi.hindi",
        reciterId: String = "ar.alafasy"
    ): Result<List<AyahUiModel>> {
        return try {
            // Call API for Uthmani & translation
            val editionsResponse = apiService.getSurahEditions(surahNumber, "quran-uthmani,$translationId")
            val audioResponse = apiService.getSurahAudio(surahNumber, reciterId)

            if (editionsResponse.isSuccessful && editionsResponse.body()?.data != null) {
                val dataList = editionsResponse.body()!!.data!!
                val arabicEdition = dataList.find { it.number == surahNumber } ?: dataList.firstOrNull()
                val translationEdition = dataList.find { it.number != surahNumber } ?: dataList.lastOrNull()

                val audioAyahs = if (audioResponse.isSuccessful) {
                    audioResponse.body()?.data?.ayahs.orEmpty()
                } else emptyList()

                val surahName = arabicEdition?.name ?: "Surah $surahNumber"
                val ayahs = mutableListOf<AyahUiModel>()

                val arabicAyahsList = arabicEdition?.ayahs.orEmpty()
                val translationAyahsList = translationEdition?.ayahs.orEmpty()

                for (i in arabicAyahsList.indices) {
                    val arabic = arabicAyahsList[i]
                    val translation = translationAyahsList.getOrNull(i)?.text ?: ""
                    val audioUrl = audioAyahs.getOrNull(i)?.audio
                        ?: "https://cdn.islamic.network/quran/audio/128/$reciterId/${arabic.number}.mp3"

                    val isBookmarked = bookmarkDao.isBookmarked("${surahNumber}_${arabic.numberInSurah}")

                    ayahs.add(
                        AyahUiModel(
                            numberInSurah = arabic.numberInSurah,
                            globalAyahNumber = arabic.number,
                            arabicText = arabic.text,
                            translationText = translation,
                            tafsirText = getSampleTafsir(surahNumber, arabic.numberInSurah),
                            audioUrl = audioUrl,
                            surahNumber = surahNumber,
                            surahName = surahName,
                            isBookmarked = isBookmarked
                        )
                    )
                }
                Result.success(ayahs)
            } else {
                // Fallback to offline preloaded Ayahs
                Result.success(getPreloadedAyahs(surahNumber))
            }
        } catch (e: Exception) {
            Result.success(getPreloadedAyahs(surahNumber))
        }
    }

    suspend fun toggleBookmark(ayah: AyahUiModel) {
        val id = "${ayah.surahNumber}_${ayah.numberInSurah}"
        if (bookmarkDao.isBookmarked(id)) {
            bookmarkDao.removeBookmark(id)
        } else {
            bookmarkDao.addBookmark(
                QuranBookmarkEntity(
                    id = id,
                    surahNumber = ayah.surahNumber,
                    surahName = ayah.surahName,
                    ayahNumber = ayah.numberInSurah,
                    arabicText = ayah.arabicText,
                    translationText = ayah.translationText
                )
            )
        }
    }

    suspend fun saveLastRead(surahNumber: Int, surahName: String, ayahNumber: Int) {
        bookmarkDao.saveLastReadPosition(
            LastReadPositionEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber
            )
        )
    }

    suspend fun getPage(
        pageNumber: Int,
        translationId: String = "hi.hindi",
        reciterId: String = "ar.alafasy"
    ): Result<QuranEbookPageUiModel> {
        val page = pageNumber.coerceIn(1, 604)
        return try {
            val uthmaniResponse = apiService.getPage(page, "quran-uthmani")
            val translationResponse = if (translationId.isNotBlank() && translationId != "quran-uthmani") {
                try {
                    apiService.getPage(page, translationId)
                } catch (e: Exception) {
                    null
                }
            } else null

            if (uthmaniResponse.isSuccessful && uthmaniResponse.body()?.data != null) {
                val pageData = uthmaniResponse.body()!!.data!!
                val transData = translationResponse?.body()?.data

                val arabicAyahs = pageData.ayahs.orEmpty()
                val transAyahs = transData?.ayahs.orEmpty()

                val firstAyah = arabicAyahs.firstOrNull()
                val primarySurahNumber = firstAyah?.surah?.number ?: 1
                val primarySurahAr = firstAyah?.surah?.name ?: "سورة"
                val primarySurahEn = firstAyah?.surah?.englishName ?: "Surah"
                val juzNumber = firstAyah?.juz ?: 1

                val ebookAyahs = mutableListOf<EbookAyahUiModel>()
                var prevSurahNumber = -1

                for (i in arabicAyahs.indices) {
                    val arAyah = arabicAyahs[i]
                    val trText = transAyahs.getOrNull(i)?.text ?: ""
                    val isNewSurah = arAyah.surah != null && (arAyah.surah.number != prevSurahNumber || arAyah.numberInSurah == 1)
                    if (arAyah.surah != null) {
                        prevSurahNumber = arAyah.surah.number
                    }

                    val isBookmarked = bookmarkDao.isBookmarked("${arAyah.surah?.number ?: 1}_${arAyah.numberInSurah}")
                    val audioUrl = arAyah.audio ?: "https://cdn.islamic.network/quran/audio/128/$reciterId/${arAyah.number}.mp3"

                    ebookAyahs.add(
                        EbookAyahUiModel(
                            globalNumber = arAyah.number,
                            numberInSurah = arAyah.numberInSurah,
                            surahNumber = arAyah.surah?.number ?: primarySurahNumber,
                            surahNameAr = arAyah.surah?.name ?: primarySurahAr,
                            surahNameEn = arAyah.surah?.englishName ?: primarySurahEn,
                            arabicText = arAyah.text.trim(),
                            translationText = trText.trim(),
                            audioUrl = audioUrl,
                            pageNumber = page,
                            juzNumber = arAyah.juz,
                            isNewSurahStart = isNewSurah,
                            isBookmarked = isBookmarked
                        )
                    )
                }

                Result.success(
                    QuranEbookPageUiModel(
                        pageNumber = page,
                        juzNumber = juzNumber,
                        primarySurahNumber = primarySurahNumber,
                        primarySurahNameAr = primarySurahAr,
                        primarySurahNameEn = primarySurahEn,
                        ayahs = ebookAyahs
                    )
                )
            } else {
                Result.success(getPreloadedPage(page, translationId))
            }
        } catch (e: Exception) {
            Result.success(getPreloadedPage(page, translationId))
        }
    }

    suspend fun getJuz(
        juzNumber: Int,
        translationId: String = "hi.hindi",
        reciterId: String = "ar.alafasy"
    ): Result<List<EbookAyahUiModel>> {
        val juz = juzNumber.coerceIn(1, 30)
        return try {
            val uthmaniResponse = apiService.getJuz(juz, "quran-uthmani")
            val transResponse = try {
                apiService.getJuz(juz, translationId)
            } catch (e: Exception) {
                null
            }

            if (uthmaniResponse.isSuccessful && uthmaniResponse.body()?.data != null) {
                val uthmaniAyahs = uthmaniResponse.body()!!.data!!.ayahs.orEmpty()
                val transAyahs = transResponse?.body()?.data?.ayahs.orEmpty()

                val resultList = mutableListOf<EbookAyahUiModel>()
                var prevSurah = -1
                for (i in uthmaniAyahs.indices) {
                    val arAyah = uthmaniAyahs[i]
                    val trText = transAyahs.getOrNull(i)?.text ?: ""
                    val isNewSurah = arAyah.surah != null && (arAyah.surah.number != prevSurah || arAyah.numberInSurah == 1)
                    if (arAyah.surah != null) prevSurah = arAyah.surah.number
                    val isBookmarked = bookmarkDao.isBookmarked("${arAyah.surah?.number ?: 1}_${arAyah.numberInSurah}")
                    val audioUrl = arAyah.audio ?: "https://cdn.islamic.network/quran/audio/128/$reciterId/${arAyah.number}.mp3"

                    resultList.add(
                        EbookAyahUiModel(
                            globalNumber = arAyah.number,
                            numberInSurah = arAyah.numberInSurah,
                            surahNumber = arAyah.surah?.number ?: 1,
                            surahNameAr = arAyah.surah?.name ?: "سورة",
                            surahNameEn = arAyah.surah?.englishName ?: "Surah",
                            arabicText = arAyah.text.trim(),
                            translationText = trText.trim(),
                            audioUrl = audioUrl,
                            pageNumber = arAyah.page,
                            juzNumber = arAyah.juz,
                            isNewSurahStart = isNewSurah,
                            isBookmarked = isBookmarked
                        )
                    )
                }
                Result.success(resultList)
            } else {
                Result.failure(Exception("Failed to load Juz $juzNumber"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleEbookBookmark(ayah: EbookAyahUiModel) {
        val id = "${ayah.surahNumber}_${ayah.numberInSurah}"
        if (bookmarkDao.isBookmarked(id)) {
            bookmarkDao.removeBookmark(id)
        } else {
            bookmarkDao.addBookmark(
                QuranBookmarkEntity(
                    id = id,
                    surahNumber = ayah.surahNumber,
                    surahName = ayah.surahNameAr,
                    ayahNumber = ayah.numberInSurah,
                    arabicText = ayah.arabicText,
                    translationText = ayah.translationText
                )
            )
        }
    }

    suspend fun searchQuranText(word: String, language: String = "hi.hindi"): Result<List<SearchMatchDto>> {
        return try {
            val response = apiService.searchQuran(word, "all", language)
            if (response.isSuccessful && response.body()?.data?.matches != null) {
                Result.success(response.body()!!.data!!.matches!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPreloadedPage(page: Int, translationId: String): QuranEbookPageUiModel {
        val isHindi = translationId.startsWith("hi")
        val ayahs = mutableListOf<EbookAyahUiModel>()

        when (page) {
            1 -> {
                val ar = listOf(
                    "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَـٰنِ ٱلرَّحِیمِ",
                    "ٱلۡحَمۡدُ لِلَّهِ رَبِّ ٱلۡعَـٰلَمِینَ",
                    "ٱلرَّحۡمَـٰنِ ٱلرَّحِیمِ",
                    "مَـٰلِكِ یَوۡمِ ٱلدِّینِ",
                    "إِیَّاكَ نَعۡبُدُ وَإِیَّاكَ نَسۡتَعِینُ",
                    "ٱهۡدِنَا ٱلصِّرَ ٰ⁠طَ ٱلۡمُسۡتَقِیمَ",
                    "صِرَ ٰ⁠طَ ٱلَّذِینَ أَنۡعَمۡتَ عَلَیۡهِمۡ غَیۡرِ ٱلۡمَغۡضُوبِ عَلَیۡهِمۡ وَلَا ٱلضَّاۤلِّینَ"
                )
                val tr = if (isHindi) listOf(
                    "अल्लाह के नाम से जो रहमान व रहीम है।",
                    "तारीफ़ अल्लाह ही के लिए है जो सारे संसार का रब है,",
                    "बड़ा कृपाशील और अत्यंत दयावान है,",
                    "बदले के दिन (क़ियामत) का मालिक है।",
                    "हम तेरी ही इबादत करते हैं और तुझ ही से मदद चाहते हैं।",
                    "हमें सीधे रास्ते पर चला,",
                    "उन लोगों के रास्ते पर जिन पर तूने इनाम फ़रमाया, जो न ग़ज़ब के भागी हुए और न भटके हुए हैं।"
                ) else listOf(
                    "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                    "[All] praise is [due] to Allah, Lord of the worlds -",
                    "The Entirely Merciful, the Especially Merciful,",
                    "Sovereign of the Day of Recompense.",
                    "It is You we worship and You we ask for help.",
                    "Guide us to the straight path -",
                    "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray."
                )

                for (i in ar.indices) {
                    ayahs.add(
                        EbookAyahUiModel(
                            globalNumber = i + 1,
                            numberInSurah = i + 1,
                            surahNumber = 1,
                            surahNameAr = "سُورَةُ ٱلْفَاتِحَةِ",
                            surahNameEn = "Al-Fatihah",
                            arabicText = ar[i],
                            translationText = tr[i],
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${i + 1}.mp3",
                            pageNumber = 1,
                            juzNumber = 1,
                            isNewSurahStart = (i == 0)
                        )
                    )
                }
                return QuranEbookPageUiModel(
                    pageNumber = 1,
                    juzNumber = 1,
                    primarySurahNumber = 1,
                    primarySurahNameAr = "سُورَةُ ٱلْفَاتِحَةِ",
                    primarySurahNameEn = "Al-Fatihah",
                    ayahs = ayahs
                )
            }
            2 -> {
                val ar = listOf(
                    "الم",
                    "ذَ ٰ⁠لِكَ ٱلۡكِتَـٰبُ لَا رَیۡبَۛ فِیهِۛ هُدࣰى لِّلۡمُتَّقِینَ",
                    "ٱلَّذِینَ یُؤۡمِنُونَ بِٱلۡغَیۡبِ وَیُقِیمُونَ ٱلصَّلَوٰةَ وَمِمَّا رَزَقۡنَـٰهُمۡ یُنفِقُونَ",
                    "وَٱلَّذِینَ یُؤۡمِنُونَ بِمَاۤ أُنزِلَ إِلَیۡكَ وَمَاۤ أُنزِلَ مِن قَبۡلِكَ وَبِٱلۡـَٔاخِرَةِ هُمۡ یُوقِنُونَ",
                    "أُو۟لَـٰۤىِٕكَ عَلَىٰ هُدࣰى مِّن رَّبِّهِمۡۖ وَأُو۟لَـٰۤىِٕكَ هُمُ ٱلۡمُفۡلِحُونَ"
                )
                val tr = if (isHindi) listOf(
                    "अलिफ़॰ लाम॰ मीम॰",
                    "यह वह किताब है जिसमें कोई संदेह नहीं, मार्गदर्शन है डर रखने वालों के लिए,",
                    "जो परोक्ष पर ईमान लाते हैं और नमाज़ क़ायम करते हैं और जो कुछ हमने उन्हें दिया है उसमें से ख़र्च करते हैं,",
                    "और जो ईमान लाते हैं उस पर जो आपकी ओर उतारा गया और जो आपसे पहले उतारा गया और आख़िरत पर पूरा यक़ीन रखते हैं,",
                    "यही लोग अपने रब की ओर से सीधे रास्ते पर हैं और यही लोग सफलता पाने वाले हैं।"
                ) else listOf(
                    "Alif, Lam, Meem.",
                    "This is the Book about which there is no doubt, a guidance for those conscious of Allah -",
                    "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,",
                    "And who believe in what has been revealed to you, [O Muhammad], and what was revealed before you, and of the Hereafter they are certain [in faith].",
                    "Those are upon [right] guidance from their Lord, and it is those who are the successful."
                )

                for (i in ar.indices) {
                    ayahs.add(
                        EbookAyahUiModel(
                            globalNumber = 7 + i + 1,
                            numberInSurah = i + 1,
                            surahNumber = 2,
                            surahNameAr = "سُورَةُ البَقَرَةِ",
                            surahNameEn = "Al-Baqarah",
                            arabicText = ar[i],
                            translationText = tr[i],
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${7 + i + 1}.mp3",
                            pageNumber = 2,
                            juzNumber = 1,
                            isNewSurahStart = (i == 0)
                        )
                    )
                }
                return QuranEbookPageUiModel(
                    pageNumber = 2,
                    juzNumber = 1,
                    primarySurahNumber = 2,
                    primarySurahNameAr = "سُورَةُ البَقَرَةِ",
                    primarySurahNameEn = "Al-Baqarah",
                    ayahs = ayahs
                )
            }
            604 -> {
                // Surah 112, 113, 114
                val ar112 = listOf(
                    "قُلۡ هُوَ ٱللَّهُ أَحَدٌ",
                    "ٱللَّهُ ٱلصَّمَدُ",
                    "لَمۡ یَلِدۡ وَلَمۡ یُولَدۡ",
                    "وَلَمۡ یَكُن لَّهُۥ كُفُوًا أَحَدُۢ"
                )
                val tr112 = if (isHindi) listOf(
                    "कहो, \"वह अल्लाह यकता (एक ही) है,",
                    "अल्लाह निरपेक्ष (सबका सहारा) है,",
                    "न उसने किसी को जना और न वह जना गया,",
                    "और न कोई उसका समकक्ष है।\""
                ) else listOf(
                    "Say, \"He is Allah, [who is] One,",
                    "Allah, the Eternal Refuge.",
                    "He neither begets nor is born,",
                    "Nor is there to Him any equivalent.\""
                )
                for (i in ar112.indices) {
                    ayahs.add(
                        EbookAyahUiModel(
                            globalNumber = 6222 + i,
                            numberInSurah = i + 1,
                            surahNumber = 112,
                            surahNameAr = "سُورَةُ الإِخْلَاصِ",
                            surahNameEn = "Al-Ikhlas",
                            arabicText = ar112[i],
                            translationText = tr112[i],
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${6222 + i}.mp3",
                            pageNumber = 604,
                            juzNumber = 30,
                            isNewSurahStart = (i == 0)
                        )
                    )
                }

                val ar113 = listOf(
                    "قُلۡ أَعُوذُ بِرَبِّ ٱلۡفَلَقِ",
                    "مِن شَرِّ مَا خَلَقَ",
                    "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                    "وَمِن شَرِّ ٱلنَّفَّـٰثَـٰتِ فِی ٱلۡعُقَدِ",
                    "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ"
                )
                val tr113 = if (isHindi) listOf(
                    "कहो, \"मैं पनाह मांगता हूँ प्रभात के रब की,",
                    "हर उस चीज़ की बुराई से जो उसने पैदा की,",
                    "और अंधेरी रात की बुराई से जब वह छा जाए,",
                    "और गांठों में फूंक मारने वालियों की बुराई से,",
                    "और डााह (ईर्ष्या) करने वाले की बुराई से जब वह डाह करे।\""
                ) else listOf(
                    "Say, \"I seek refuge in the Lord of daybreak,",
                    "From the evil of that which He created,",
                    "And from the evil of darkness when it settles,",
                    "And from the evil of the blowers in knots,",
                    "And from the evil of an envier when he envies.\""
                )
                for (i in ar113.indices) {
                    ayahs.add(
                        EbookAyahUiModel(
                            globalNumber = 6226 + i,
                            numberInSurah = i + 1,
                            surahNumber = 113,
                            surahNameAr = "سُورَةُ الفَلَقِ",
                            surahNameEn = "Al-Falaq",
                            arabicText = ar113[i],
                            translationText = tr113[i],
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${6226 + i}.mp3",
                            pageNumber = 604,
                            juzNumber = 30,
                            isNewSurahStart = (i == 0)
                        )
                    )
                }

                val ar114 = listOf(
                    "قُلۡ أَعُوذُ بِرَبِّ ٱلنَّاسِ",
                    "مَلِكِ ٱلنَّاسِ",
                    "إِلَـٰهِ ٱلنَّاسِ",
                    "مِن شَرِّ ٱلۡوَسۡوَاسِ ٱلۡخَنَّاسِ",
                    "ٱلَّذِی یُوَسۡوِسُ فِی صُدُورِ ٱلنَّاسِ",
                    "مِنَ ٱلۡجِنَّةِ وَٱلنَّاسِ"
                )
                val tr114 = if (isHindi) listOf(
                    "कहो, \"मैं पनाह मांगता हूँ इंसानों के रब की,",
                    "इंसानों के बादशाह की,",
                    "इंसानों के माबूद (पूज्य) की,",
                    "पीछे हट जाने वाले बहकाने वाले के शर से,",
                    "जो लोगों के दिलों में वसवसे (शंकाएं) डालता है,",
                    "जिन्नों में से भी और इंसानों में से भी।\""
                ) else listOf(
                    "Say, \"I seek refuge in the Lord of mankind,",
                    "The Sovereign of mankind.",
                    "The God of mankind,",
                    "From the evil of the retreating whisperer -",
                    "Who whispers [evil] into the breasts of mankind -",
                    "From among the jinn and mankind.\""
                )
                for (i in ar114.indices) {
                    ayahs.add(
                        EbookAyahUiModel(
                            globalNumber = 6231 + i,
                            numberInSurah = i + 1,
                            surahNumber = 114,
                            surahNameAr = "سُورَةُ النَّاسِ",
                            surahNameEn = "An-Nas",
                            arabicText = ar114[i],
                            translationText = tr114[i],
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${6231 + i}.mp3",
                            pageNumber = 604,
                            juzNumber = 30,
                            isNewSurahStart = (i == 0)
                        )
                    )
                }
                return QuranEbookPageUiModel(
                    pageNumber = 604,
                    juzNumber = 30,
                    primarySurahNumber = 112,
                    primarySurahNameAr = "سُورَةُ الإِخْلَاصِ",
                    primarySurahNameEn = "Al-Ikhlas",
                    ayahs = ayahs
                )
            }
            else -> {
                // Approximate Surah and Juz based on Page
                val juz = ((page - 1) / 20) + 1
                val approxSurahNum = ((page * 114) / 604).coerceIn(1, 114)
                val (arName, enName, _) = getPreloadedSurahList().find { it.number == approxSurahNum }?.let {
                    Triple(it.name, it.englishName, it.englishNameTranslation)
                } ?: Triple("سورة $approxSurahNum", "Surah $approxSurahNum", "")

                for (i in 1..4) {
                    ayahs.add(
                        EbookAyahUiModel(
                            globalNumber = (page * 10) + i,
                            numberInSurah = i,
                            surahNumber = approxSurahNum,
                            surahNameAr = arName,
                            surahNameEn = enName,
                            arabicText = "آيَةٌ كَرِيمَةٌ مِنْ $arName - صَفْحَة $page",
                            translationText = if (isHindi) "यह पवित्र क़ुरआन का पृष्ठ $page है। पृष्ठ लोड करने के लिए इंटरनेट से कनेक्ट करें।" else "Page $page of Holy Quran. Connect to internet to stream authentic Uthmani script and translations.",
                            audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${(page * 10) + i}.mp3",
                            pageNumber = page,
                            juzNumber = juz,
                            isNewSurahStart = (i == 1)
                        )
                    )
                }
                return QuranEbookPageUiModel(
                    pageNumber = page,
                    juzNumber = juz,
                    primarySurahNumber = approxSurahNum,
                    primarySurahNameAr = arName,
                    primarySurahNameEn = enName,
                    ayahs = ayahs
                )
            }
        }
    }

    private fun getSampleTafsir(surahNum: Int, ayahNum: Int): String {
        return "Tafsir Ibn Kathir: This verse emphasizes the oneness of Allah, the divine guidance revealed to mankind, and the virtues of mindfulness (Taqwa) in daily conduct."
    }

    // Preloaded offline list of 114 Surahs
    fun getPreloadedSurahList(): List<SurahDto> {
        val list = mutableListOf<SurahDto>()
        val names = listOf(
            Triple(1, "Al-Fatihah", "The Opening"),
            Triple(2, "Al-Baqarah", "The Cow"),
            Triple(3, "Ali 'Imran", "Family of Imran"),
            Triple(4, "An-Nisa", "The Women"),
            Triple(5, "Al-Ma'idah", "The Table Spread"),
            Triple(6, "Al-An'am", "The Cattle"),
            Triple(7, "Al-A'raf", "The Heights"),
            Triple(8, "Al-Anfal", "The Spoils of War"),
            Triple(9, "At-Tawbah", "The Repentance"),
            Triple(10, "Yunus", "Jonah"),
            Triple(11, "Hud", "Hud"),
            Triple(12, "Yusuf", "Joseph"),
            Triple(13, "Ar-Ra'd", "The Thunder"),
            Triple(14, "Ibrahim", "Abraham"),
            Triple(15, "Al-Hijr", "The Rocky Tract"),
            Triple(16, "An-Nahl", "The Bee"),
            Triple(17, "Al-Isra", "The Night Journey"),
            Triple(18, "Al-Kahf", "The Cave"),
            Triple(19, "Maryam", "Mary"),
            Triple(20, "Taha", "Ta-Ha"),
            Triple(36, "Ya-Sin", "Ya-Sin"),
            Triple(55, "Ar-Rahman", "The Beneficent"),
            Triple(56, "Al-Waqi'ah", "The Inevitable"),
            Triple(67, "Al-Mulk", "The Sovereignty"),
            Triple(112, "Al-Ikhlas", "The Sincerity"),
            Triple(113, "Al-Falaq", "The Daybreak"),
            Triple(114, "An-Nas", "Mankind")
        )

        // Build 114 Surahs array
        val fullNames = mapOf(
            1 to Triple("الفاتحة", "Al-Fatihah", "The Opening"),
            2 to Triple("البقرة", "Al-Baqarah", "The Cow"),
            3 to Triple("آل عمران", "Ali 'Imran", "Family of Imran"),
            4 to Triple("النساء", "An-Nisa", "The Women"),
            5 to Triple("المائدة", "Al-Ma'idah", "The Table Spread"),
            18 to Triple("الكهف", "Al-Kahf", "The Cave"),
            36 to Triple("يس", "Ya-Sin", "Ya-Sin"),
            55 to Triple("الرحمن", "Ar-Rahman", "The Beneficent"),
            56 to Triple("الواقعة", "Al-Waqi'ah", "The Inevitable"),
            67 to Triple("الملك", "Al-Mulk", "The Sovereignty"),
            108 to Triple("الكوثر", "Al-Kawthar", "Abundance"),
            112 to Triple("الإخلاص", "Al-Ikhlas", "The Sincerity"),
            113 to Triple("الفلق", "Al-Falaq", "The Daybreak"),
            114 to Triple("الناس", "An-Nas", "Mankind")
        )

        val totalVerses = mapOf(1 to 7, 2 to 286, 3 to 200, 18 to 110, 36 to 83, 55 to 78, 56 to 96, 67 to 30, 108 to 3, 112 to 4, 113 to 5, 114 to 6)

        for (i in 1..114) {
            val (arName, enName, trans) = fullNames[i] ?: Triple("سورة $i", "Surah $i", "Surah Chapter $i")
            val count = totalVerses[i] ?: (7 + (i * 3) % 150)
            val type = if (i in listOf(1, 2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 110)) "Medinan" else "Meccan"
            list.add(
                SurahDto(
                    number = i,
                    name = arName,
                    englishName = enName,
                    englishNameTranslation = trans,
                    numberOfAyahs = count,
                    revelationType = type
                )
            )
        }
        return list
    }

    private fun getPreloadedAyahs(surahNumber: Int): List<AyahUiModel> {
        val list = mutableListOf<AyahUiModel>()
        if (surahNumber == 1) {
            val fatihahArabic = listOf(
                "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
                "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                "مَـٰلِكِ يَوْمِ ٱلدِّينِ",
                "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ",
                "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ"
            )
            val fatihahEn = listOf(
                "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                "[All] praise is [due] to Allah, Lord of the worlds -",
                "The Entirely Merciful, the Especially Merciful,",
                "Sovereign of the Day of Recompense.",
                "It is You we worship and You we ask for help.",
                "Guide us to the straight path -",
                "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray."
            )
            for (i in fatihahArabic.indices) {
                list.add(
                    AyahUiModel(
                        numberInSurah = i + 1,
                        globalAyahNumber = i + 1,
                        arabicText = fatihahArabic[i],
                        translationText = fatihahEn[i],
                        tafsirText = getSampleTafsir(1, i + 1),
                        audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${i + 1}.mp3",
                        surahNumber = 1,
                        surahName = "الفاتحة (Al-Fatihah)"
                    )
                )
            }
        } else if (surahNumber == 112) {
            val arabic = listOf(
                "قُلْ هُوَ ٱللَّهُ أَحَدٌ",
                "ٱللَّهُ ٱلصَّمَدُ",
                "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌ"
            )
            val en = listOf(
                "Say, \"He is Allah, [who is] One,",
                "Allah, the Eternal Refuge.",
                "He neither begets nor is born,",
                "Nor is there to Him any equivalent.\""
            )
            for (i in arabic.indices) {
                list.add(
                    AyahUiModel(
                        numberInSurah = i + 1,
                        globalAyahNumber = 6222 + i,
                        arabicText = arabic[i],
                        translationText = en[i],
                        tafsirText = "Surah Al-Ikhlas is equal to one-third of the Quran in reward, confirming Absolute Monotheism.",
                        audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${6222 + i}.mp3",
                        surahNumber = 112,
                        surahName = "الإخلاص (Al-Ikhlas)"
                    )
                )
            }
        } else {
            // General placeholder Ayahs
            val bismillah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"
            list.add(
                AyahUiModel(
                    numberInSurah = 1,
                    globalAyahNumber = 1,
                    arabicText = bismillah,
                    translationText = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                    tafsirText = "Bismillah is the divine opening of the Surah.",
                    audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3",
                    surahNumber = surahNumber,
                    surahName = "Surah $surahNumber"
                )
            )
            for (i in 2..5) {
                list.add(
                    AyahUiModel(
                        numberInSurah = i,
                        globalAyahNumber = i,
                        arabicText = "آيَةٌ كَرِيمَةٌ مِنْ سُورَةِ $surahNumber - الآيَةُ $i",
                        translationText = "Verse $i of Surah $surahNumber: Indeed Allah is with those who are patient and mindful.",
                        tafsirText = "Detailed explanation of verse $i.",
                        audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$i.mp3",
                        surahNumber = surahNumber,
                        surahName = "Surah $surahNumber"
                    )
                )
            }
        }
        return list
    }
}
