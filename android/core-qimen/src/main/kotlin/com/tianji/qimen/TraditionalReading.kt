package com.tianji.qimen

/** A deliberately small, auditable traditional reading, not a learned prediction. */
data class TraditionalReading(val palace: Palace, val stem: String, val tone: Int, val themeZh: String,
    val themeEn: String, val meaningZh: String, val meaningEn: String, val stepZh: String, val stepEn: String) {
    companion object {
        fun from(chart: Chart): TraditionalReading {
            require(chart.status == "TRADITIONAL" && chart.ruleset.id == ShiJiaChaiBu.metadata.id)
            val stem = chart.calendar.getValue("hour_focus_stem")
            val p = chart.palaces.single { it.heaven_stem?.split("、")?.contains(stem) == true }
            val reading = when (p.door) {
                "開門" -> listOf("較宜開展", "Opening", "開門的傳統象意偏向開展與接觸；本盤可從主動溝通、小規模嘗試着手。", "The Open Door traditionally suggests beginnings and contact. Consider a conversation or a small trial.", "先提出一個清楚請求，確認回應後再決定下一步。", "Make one clear request, then decide the next step from the response.")
                "休門" -> listOf("較宜調整", "Regrouping", "休門的傳統象意偏向休整與和緩；本盤可先整理狀態，再溫和推進。", "The Rest Door traditionally suggests recuperation and a gentle pace. Regroup before proceeding.", "留一小段空檔，整理資料或約一場輕鬆對話。", "Leave a short pause to organize information or have a relaxed conversation.")
                "生門" -> listOf("較宜累積", "Building", "生門的傳統象意偏向生長與累積；本盤可把注意力放在可持續的小進展。", "The Life Door traditionally suggests growth and accumulation. Focus on a small, sustainable improvement.", "完成一項可重複的小練習，記低實際進度。", "Complete one repeatable practice and record the actual progress.")
                "杜門" -> listOf("宜先準備", "Preparation", "杜門的傳統象意偏向閉藏與阻隔；本盤可先做準備、釐清界線。", "The Concealment Door traditionally suggests privacy and barriers. Prepare and clarify boundaries.", "先核對資料及必要條件，未清楚的地方留待確認。", "Check the information and prerequisites; confirm what remains unclear.")
                "景門" -> listOf("留意表達", "Presentation", "景門的傳統象意偏向文書與呈現；本盤可留意表達是否清楚、內容是否有根據。", "The View Door traditionally suggests writing and presentation. Check clarity and supporting facts.", "寫一段簡短草稿，核實內容後再分享。", "Write a short draft and verify it before sharing.")
                "傷門" -> listOf("宜放慢核對", "Proceed carefully", "傷門的傳統象意帶有衝突與損耗；本盤提示先核對分歧，避免急着推進。", "The Harm Door traditionally suggests conflict or strain. Check disagreements before pushing ahead.", "把分歧寫清楚，以一個低成本小步確認情況。", "Write down the disagreement and check it with a low-cost step.")
                "死門" -> listOf("宜整理收尾", "Closure", "死門的傳統象意偏向停滯與終結；本盤可用作檢視舊安排、整理未完成事項。這個名稱不代表死亡預告。", "The Death Door traditionally suggests closure or stillness. Review old arrangements and unfinished tasks. Its name is not a prediction of death.", "先完成一件未完的小事，再評估是否展開新安排。", "Close one unfinished task before considering a new commitment.")
                "驚門" -> listOf("宜先確認", "Clarification", "驚門的傳統象意偏向驚擾與爭議；本盤可先分清已知事實與猜測。", "The Alarm Door traditionally suggests disturbance or dispute. Separate established facts from assumptions.", "有不清楚的消息，先向可靠來源核對一次。", "Verify one uncertain claim with a reliable source.")
                else -> error("Unsupported door")
            }
            val tone = when (p.door) { "開門", "休門", "生門" -> 1; "杜門", "景門" -> 0; else -> -1 }
            return TraditionalReading(p, stem, tone, reading[0], reading[1], reading[2], reading[3], reading[4], reading[5])
        }
    }
}
