package com.tianji.domain

import java.time.Instant
import java.time.ZoneId

data class CityZone(val id: String, val chinese: String, val english: String, val aliases: String = "")

object TimeZones {
    val common = listOf(
        CityZone("Asia/Hong_Kong", "香港", "Hong Kong", "香港 港 hongkong"),
        CityZone("America/Los_Angeles", "西雅圖／洛杉磯（美國太平洋時間）", "Seattle / Los Angeles (US Pacific)", "西雅图 洛杉矶 Seattle Los Angeles Renton 雷頓 雷顿 Pacific"),
        CityZone("America/Vancouver", "溫哥華", "Vancouver", "温哥华"),
        CityZone("Asia/Taipei", "台北", "Taipei", "臺北 台灣 台湾"),
        CityZone("Asia/Shanghai", "北京／上海", "Beijing / Shanghai", "中國 中国 China"),
        CityZone("Asia/Singapore", "新加坡", "Singapore"),
        CityZone("Asia/Tokyo", "東京", "Tokyo", "东京 日本 Japan"),
        CityZone("Asia/Seoul", "首爾", "Seoul", "首尔 韓國 韩国 Korea"),
        CityZone("Asia/Bangkok", "曼谷", "Bangkok", "泰國 泰国 Thailand"),
        CityZone("Australia/Sydney", "悉尼", "Sydney", "雪梨 澳洲 Australia"),
        CityZone("Europe/London", "倫敦", "London", "伦敦 英國 英国 UK"),
        CityZone("Europe/Paris", "巴黎", "Paris", "法國 法国 France"),
        CityZone("America/New_York", "紐約（美國東岸時間）", "New York (US Eastern)", "纽约 Eastern"),
        CityZone("America/Chicago", "芝加哥（美國中部時間）", "Chicago (US Central)", "Central"),
        CityZone("America/Denver", "丹佛（美國山區時間）", "Denver (US Mountain)", "Mountain"),
        CityZone("America/Phoenix", "鳳凰城", "Phoenix", "凤凰城 Arizona"),
        CityZone("Pacific/Honolulu", "檀香山（夏威夷）", "Honolulu (Hawaii)", "Hawaii"),
        CityZone("UTC", "協調世界時間", "Coordinated Universal Time", "世界时间")
    )

    fun offset(id: String, at: Instant): String {
        val offset = ZoneId.of(id).rules.getOffset(at).id
        return "UTC" + if (offset == "Z") "+00:00" else offset
    }

    // This is the device's current zone, saved as a choice, not a GPS lookup.
    fun deviceZone(): String = ZoneId.systemDefault().id

    fun matches(zone: CityZone, query: String, localizedCity: String = ""): Boolean {
        val haystack = "${zone.id} ${zone.chinese} ${zone.english} ${zone.aliases} $localizedCity"
        return query.trim().split(Regex("\\s+")).all { haystack.contains(it, ignoreCase = true) }
    }
}
