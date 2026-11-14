package com.example.data.repository

import com.example.data.models.HistoricalArticle
import com.example.data.models.HistoricalPhase

class HistoryRepository {

    val phases: List<HistoricalPhase> = listOf(
        HistoricalPhase(
            id = "phase1",
            phaseNumber = 1,
            title = "Pre-Islamic Arabia & Early Life",
            titleArabic = "الجاهلية والمولد الشريف",
            eraPeriod = "570–610 CE",
            summary = "The social context of tribal Arabia, the Year of the Elephant, the birth of Prophet Muhammad ﷺ in Makkah, Al-Amin reputation, and marriage to Khadijah (RA).",
            keyEventsCount = 4,
            accentColorHex = "#0A3A2A"
        ),
        HistoricalPhase(
            id = "phase2",
            phaseNumber = 2,
            title = "The Revelation & Meccan Era",
            titleArabic = "البعثة والعهد المكي",
            eraPeriod = "610–622 CE",
            summary = "First revelation in Cave Hira, early secret preaching, boycott in She'eb Abi Talib, Year of Sorrow, Isra and Mi'raj night journey, and Pledges of Aqabah.",
            keyEventsCount = 5,
            accentColorHex = "#1C4E3D"
        ),
        HistoricalPhase(
            id = "phase3",
            phaseNumber = 3,
            title = "The Hijrah & Medina State",
            titleArabic = "الهجرة وتأسيس الدولة",
            eraPeriod = "622–632 CE",
            summary = "Migration to Yathrib (Medina), Constitution of Medina, Major Battles (Badr, Uhud, Khandaq), Treaty of Hudaybiyyah, Conquest of Makkah, and Farewell Sermon.",
            keyEventsCount = 6,
            accentColorHex = "#2E6250"
        ),
        HistoricalPhase(
            id = "phase4",
            phaseNumber = 4,
            title = "Rashidun & The Golden Age",
            titleArabic = "الخلافة الراشدة والعصر الذهبي",
            eraPeriod = "632–1258 CE",
            summary = "Leadership of the Rightly-Guided Caliphs (Abu Bakr, Umar, Uthman, Ali R.A.), compilation of Quran, rapid expansion, House of Wisdom in Baghdad, and scientific achievements.",
            keyEventsCount = 6,
            accentColorHex = "#D4AF37"
        )
    )

    val articles: List<HistoricalArticle> = listOf(
        HistoricalArticle(
            id = "badr",
            phaseId = "phase3",
            title = "The Battle of Badr",
            titleArabic = "غزوة بدر الكبرى",
            dateOrYear = "17 Ramadan, 2 AH (624 CE)",
            location = "Badr Wells, Hejaz region",
            overview = "The decisive turning point in early Islamic history where 313 Muslims defeated a Quraysh army thrice their size through divine aid and tactical wisdom.",
            detailedContent = """
                The Battle of Badr was the first large-scale military engagement between Muslims and the Quraysh tribe of Makkah. Taking place at the wells of Badr, 80 miles southwest of Medina, the Muslim force comprised 313 men with minimal armor and only two horses.

                Key Events & Dynamics:
                1. Tactical Positioning: Hubab ibn al-Mundhir suggested controlling the water sources of Badr, securing a vital strategic advantage.
                2. Divine Assistance: Surah Ali 'Imran (3:123-125) commemorates the angels sent down to reinforce the believers.
                3. Leadership & Unity: Prophet Muhammad ﷺ spent the night in intense supplication inside his tent (Arish), seeking victory for the small community.

                Impact:
                The victory established Medina as a formidable sovereign power in the Arabian Peninsula, boosted Muslim morale, and laid down the humane rules of engagement regarding prisoners of war.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Faith and reliance on Allah (Tawakkul) outweigh numerical disadvantage.",
                "Consultation (Shura) was practiced by the Prophet ﷺ in military strategy.",
                "Captives were treated mercifully; educated prisoners were freed upon teaching 10 Muslim youth to read."
            ),
            mapRegionUrl = "Badr - Western Arabia Trade Corridor"
        ),
        HistoricalArticle(
            id = "uhud",
            phaseId = "phase3",
            title = "The Battle of Uhud",
            titleArabic = "غزوة أحد",
            dateOrYear = "3 AH (625 CE)",
            location = "Mount Uhud, Medina",
            overview = "A profound lesson in discipline and obedience when a Qurayshi retaliation force confronted Muslims north of Medina.",
            detailedContent = """
                Seeking revenge for Badr, the Quraysh assembled an army of 3,000 men led by Abu Sufyan. Prophet Muhammad ﷺ deployed 50 archers on Mount Rumat under Abdullah ibn Jubayr with strict instructions not to leave their post under any circumstances.

                Key Phases:
                1. Initial Advantage: The Muslim ranks initially repelled the Quraysh infantry.
                2. Premature Desertion: Seeing the retreat, 40 archers abandoned their hill post to gather war booty, leaving the rear vulnerable.
                3. Cavalry Flank: Khalid ibn al-Walid capitalized on the opening, leading a decisive cavalry strike from behind.
                4. Heroism of Sahabah: Seventy companions, including Hamza ibn Abdul-Muttalib (RA) and Mus'ab ibn Umair (RA), attained martyrdom defending the Prophet ﷺ.

                Profound Lessons:
                Surah Ali 'Imran reflected upon Uhud, highlighting that spiritual discipline and obedience to command are imperative for true success.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Unwavering obedience to divine and leader instructions is vital.",
                "Material greed compromises tactical strength and unity.",
                "Martyrdom of Hamza (RA) 'Leader of Martyrs'."
            ),
            mapRegionUrl = "Mount Uhud - North Medina Oasis"
        ),
        HistoricalArticle(
            id = "khandaq",
            phaseId = "phase3",
            title = "Battle of the Trench (Al-Khandaq)",
            titleArabic = "غزوة الخندق (الأحزاب)",
            dateOrYear = "5 AH (627 CE)",
            location = "Northern Border of Medina",
            overview = "An innovative defensive siege strategy proposed by Salman al-Farsi (RA) that thwarted a confederation of 10,000 enemy troops.",
            detailedContent = """
                A coalition of Quraysh, Ghatafan, and surrounding tribes assembled 10,000 soldiers to eradicate Medina. Recognizing the existential threat, Prophet Muhammad ﷺ accepted the proposal of Salman al-Farsi (RA) to dig a wide trench along Medina's exposed northern perimeter.

                Key Elements:
                1. Digging the Trench: The Prophet ﷺ personally swung the pickaxe alongside the Sahabah in severe cold and famine.
                2. Psychological Warfare: A month-long siege where cold winds, supply exhaustion, and internal covert intelligence (Nu'aym ibn Mas'ud) fractured coalition trust.
                3. Divine Miracles: Multiplied food for the diggers and a fierce windstorm that blew away enemy tents and cooking pots.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Embracing innovation and cross-cultural ideas (Persian engineering) in defense.",
                "Resilience during prolonged economic and psychological siege.",
                "Strategic intelligence and diplomatic maneuver."
            ),
            mapRegionUrl = "Northern Medina Perimeter Trench"
        ),
        HistoricalArticle(
            id = "hudaybiyyah",
            phaseId = "phase3",
            title = "The Treaty of Hudaybiyyah",
            titleArabic = "صلح الحديبية",
            dateOrYear = "6 AH (628 CE)",
            location = "Al-Hudaybiyyah, Outskirts of Makkah",
            overview = "A masterclass in peace treaty diplomacy that Allah revealed as 'A Manifest Victory' (Fath Mubeen).",
            detailedContent = """
                Prophet Muhammad ﷺ led 1,400 unarmed believers dressed in Ihram towards Makkah for Umrah. Blocked at Hudaybiyyah, peace negotiations ensued with Suhayl ibn Amr representing Quraysh.

                Terms & Controversy:
                1. 10-Year Cessation of Hostilities.
                2. Muslims return to Medina without Umrah this year, permitted for 3 days next year.
                3. Asymmetrical extradition clause initially displeasing to some companions.

                Fruitful Outcome:
                During the 2 years of peace following Hudaybiyyah, more people accepted Islam (including Khalid ibn al-Walid and Amr ibn al-As) than in the previous 18 years combined.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Peaceful diplomacy opens hearts far faster than warfare.",
                "Patience in long-term vision over short-term pride.",
                "Direct precursor to the peaceful Conquest of Makkah."
            ),
            mapRegionUrl = "Makkah - Hudaybiyyah Plain"
        ),
        HistoricalArticle(
            id = "sermon",
            phaseId = "phase3",
            title = "The Farewell Sermon (Khutbat al-Wada')",
            titleArabic = "خطبة حجة الوداع",
            dateOrYear = "9 Dhul Hijjah, 10 AH (632 CE)",
            location = "Mount Arafat (Jabal al-Rahmah)",
            overview = "The universal declaration of human rights, racial equality, women's dignity, and economic justice delivered to 120,000 pilgrims.",
            detailedContent = """
                During his final Hajj pilgrimage, Prophet Muhammad ﷺ delivered a timeless address outlining the foundational moral Charter of Islam:

                Core Principles Declared:
                1. Absolute Racial Equality: "An Arab has no superiority over a non-Arab, nor a non-Arab over an Arab... except by piety (Taqwa)."
                2. Sanctity of Life & Property: Human life, honor, and wealth are inviolable sacred trusts.
                3. Rights of Women: "Treat women with kindness and fairness, for they are your partners and committed helpers."
                4. Prohibition of Usury (Riba) & Blood Feuds: Eradication of predatory interest and tribal vendettas.
                5. Completion of Religion: The revelation of verse 5:3: "This day I have perfected for you your religion..."
            """.trimIndent(),
            keyTakeaways = listOf(
                "Universal human rights and anti-racism charter.",
                "Eradication of economic exploitation (Riba).",
                "Protection and honor of women's rights."
            ),
            mapRegionUrl = "Arafat Plain - Jabal al-Rahmah"
        ),
        HistoricalArticle(
            id = "golden_age",
            phaseId = "phase4",
            title = "The Islamic Golden Age & Bayt al-Hikmah",
            titleArabic = "العصر الذهبي بيت الحكمة",
            dateOrYear = "8th–14th Century CE (750–1258 CE)",
            location = "Baghdad, Cordoba, Cairo, Samarkand",
            overview = "An era of unparalleled scientific, medical, mathematical, and philosophical flourishing centered at the House of Wisdom in Baghdad.",
            detailedContent = """
                Triggered by the Quranic directive to seek knowledge (Iqra) and observe the natural cosmos, scholars of diverse faiths gathered across Baghdad, Cairo, and Andalusian Cordoba.

                Luminaries & Breakthroughs:
                1. Al-Khwarizmi: Inventor of Algebra (Al-Jabr) and algorithms enabling modern computing.
                2. Ibn Sina (Avicenna): Author of 'The Canon of Medicine', standard medical text in Europe for 500 years.
                3. Al-Hassan Ibn al-Haytham: Father of Optics and developer of the modern scientific experimental method.
                4. Miriam al-Asturlabi: Master maker of complex astronomical Astrolabes.
                5. Fatima al-Fihri: Founder of the world's oldest degree-granting university, Al-Qarawiyyin in Fez, Morocco (859 CE).
            """.trimIndent(),
            keyTakeaways = listOf(
                "Seamless harmony between faith, intellect, and empirical science.",
                "Preservation and expansion of Greek, Indian, and Persian knowledge.",
                "Foundational roots of modern medicine, algebra, and optics."
            ),
            mapRegionUrl = "Trans-Eurasian Golden Age Knowledge Hubs"
        )
    )
}
