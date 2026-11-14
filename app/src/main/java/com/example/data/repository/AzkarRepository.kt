package com.example.data.repository

import com.example.data.models.AzkarItem

class AzkarRepository {

    fun getAzkarByCategory(category: String): List<AzkarItem> {
        val all = getAllAzkar()
        return all.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getAllAzkar(): List<AzkarItem> {
        return listOf(
            AzkarItem(
                id = 1,
                category = "Morning",
                arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ",
                translation = "We have reached the morning and at this time all sovereignty belongs to Allah, praise is to Allah.",
                reference = "Sahih Muslim",
                targetCount = 1
            ),
            AzkarItem(
                id = 2,
                category = "Morning",
                arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                translation = "O Allah, by Your leave we enter the morning and by Your leave we enter the evening, by You we live and by You we die, and unto You is the resurrection.",
                reference = "Sunan Abu Dawud",
                targetCount = 1
            ),
            AzkarItem(
                id = 3,
                category = "Morning",
                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
                translation = "Glory is to Allah and praise is to Him, by the number of His creation and His pleasure and the weight of His Throne.",
                reference = "Sahih Muslim",
                targetCount = 3
            ),
            AzkarItem(
                id = 4,
                category = "Evening",
                arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ",
                translation = "We have reached the evening and at this time all sovereignty belongs to Allah.",
                reference = "Sahih Muslim",
                targetCount = 1
            ),
            AzkarItem(
                id = 5,
                category = "Evening",
                arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                translation = "I seek refuge in the Perfect Words of Allah from the evil of what He has created.",
                reference = "Sahih Muslim",
                targetCount = 3
            ),
            AzkarItem(
                id = 6,
                category = "After Prayer",
                arabicText = "أَسْتَغْفِرُ اللَّهَ (٣x) - اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ",
                translation = "I seek forgiveness from Allah (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of Majesty and Honor.",
                reference = "Sahih Muslim",
                targetCount = 1
            ),
            AzkarItem(
                id = 7,
                category = "After Prayer",
                arabicText = "سُبْحَانَ اللَّهِ (٣٣x) ، اَلْحَمْدُ لِلَّهِ (٣٣x) ، اَللَّهُ أَكْبَرُ (٣٣x)",
                translation = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (33x) - Glorified is Allah, Praise be to Allah, Allah is Greatest.",
                reference = "Sahih Al-Bukhari",
                targetCount = 33
            ),
            AzkarItem(
                id = 8,
                category = "Sleep",
                arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا",
                translation = "In Your name, my Lord, I lie down, and in Your name I rise. If You take my soul, have mercy upon it.",
                reference = "Sahih Al-Bukhari",
                targetCount = 1
            )
        )
    }
}
