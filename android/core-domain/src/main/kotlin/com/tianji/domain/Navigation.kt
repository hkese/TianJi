package com.tianji.domain

import com.tianji.qimen.*
import kotlinx.serialization.Serializable
import java.time.Instant

/** Product-authored practical recipes. The door is a disclosed symbolic lens, not a score. */
@Serializable data class ActionGuide(
    val version: String = "1.0", val titleEn: String, val focusDoor: String,
    val stepsZh: List<String>, val stepsEn: List<String>, val doneZh: String, val doneEn: String,
    val fitZh: String, val fitEn: String
)

object Navigator {
    fun candidates(goal: Goal, plan: PlanVersion, snapshot: ChartSnapshot, now: Instant): List<ActionCandidate> {
        val base = Planner.candidates(goal, plan, snapshot, now)
        val chart = Codec.decodeFromString<Chart>(snapshot.chartJson)
        require(chart.chart_id == snapshot.id && chart.input_hash == snapshot.inputHash)
        require(chart.status == "TRADITIONAL" && chart.ruleset.id == ShiJiaChaiBu.metadata.id)
        val g = goal.title
        val recipes = when (goal.purpose) {
            "WORK" -> listOf(
                recipe("整理一個職位／工作要求", "Clarify one work opportunity", "開門", "未清楚對方需要甚麼", "When the requirements are unclear",
                    listOf("打開你已有的一份職位資料或工作要求；未有資料就寫低要問的人。", "就「$g」列出對方最重視的三項要求，逐項標示自己已有的證據。", "寫低一條要核實的問題；只做草稿，確認資料後再決定是否聯絡。"),
                    listOf("Open one existing job description or work brief; otherwise name someone who can clarify it.", "For ‘$g’, list three requirements and the evidence you already have for each.", "Draft one question to verify. Review the facts before deciding whether to contact anyone."), "一份三項要求清單＋一條問題", "Three requirements and one question"),
                recipe("寫好三句自我介紹", "Draft a three-line introduction", "景門", "知道機會，但唔知點表達", "When you need to present your experience",
                    listOf("寫一句：我想為「$g」解決甚麼問題。", "寫一句真實做過的例子，再寫一句希望對方提供的下一步。", "大聲讀一次，刪走誇大或未核實內容，存成草稿。"),
                    listOf("Write one sentence about the problem you want to solve through ‘$g’.", "Add one real example of your experience and one specific next-step request.", "Read it aloud, remove unsupported claims, and save the draft."), "三句可核實的介紹草稿", "A factual three-line introduction"),
                contact(g, "揀一個人，準備請教", "Prepare to ask one person"))
            "STUDY" -> listOf(
                recipe("讀一小段，寫一句重點", "Read a passage and capture one idea", "景門", "需要先理解內容", "When you need to understand the material",
                    listOf("打開手上與「$g」最相關的一頁或一小段教材。", "只讀呢一段，圈出一個關鍵概念；唔明的字先標記。", "合上教材，用自己的說話寫一句重點，再翻開核對。"),
                    listOf("Open one page or short passage relevant to ‘$g’ using material you already have.", "Read only that passage and mark one key idea and any unclear terms.", "Close it, write one takeaway in your own words, then check it."), "一句重點＋一個待釐清問題", "One takeaway and one open question"),
                recipe("做一題，核對一個錯誤", "Try one exercise and check it", "生門", "已睇過，想變成自己識用", "When you want to turn knowledge into practice",
                    listOf("為「$g」揀一道已有答案的練習，或重做一個教材例子。", "先自己試，卡住就記低卡在哪一步；時間到就停。", "對照答案，寫低一項錯誤或一項確認學識的技巧。"),
                    listOf("Choose one exercise with a solution, or a worked example relevant to ‘$g’.", "Try it yourself. Mark where you get stuck and stop when the window ends.", "Compare with the solution and note one correction or skill learned."), "一個嘗試記錄＋一項修正", "One attempt and one correction"),
                contact(g, "整理一條問題去請教", "Prepare one learning question"))
            "TALK" -> listOf(
                recipe("先寫一段唔指責嘅訊息", "Draft a non-blaming message", "景門", "有情緒，想先整理講法", "When you want to organize your words",
                    listOf("就「$g」寫低一件你親身觀察到的事，唔猜對方動機。", "加一句自己的感受／需要，再加一個對方可以拒絕的具體請求。", "讀一次，刪走「你一定」「你成日」等指責字眼，先存草稿。"),
                    listOf("For ‘$g’, write one thing you directly observed without guessing motives.", "Add your need or feeling and a specific request the other person can decline.", "Remove blaming generalizations and save a draft before deciding whether to send."), "事實、需要、請求各一句", "One sentence each for observation, need and request"),
                contact(g, "準備一個輕鬆對話邀請", "Prepare a low-pressure invitation"),
                recipe("列清楚想傾嘅一件事", "Define one topic and boundary", "開門", "想展開對話，但議題太多", "When there are too many topics",
                    listOf("將「$g」縮成今次只處理的一件事。", "列出一個可接受安排，以及一個自己暫時不能答應的界線。", "寫低開場問題：「你願唔願意用幾分鐘一齊傾呢件事？」對方拒絕就暫停。"),
                    listOf("Reduce ‘$g’ to one topic for this conversation.", "Write one acceptable arrangement and one boundary you need to keep.", "Draft an invitation to discuss it briefly. Pause if the other person declines."), "一個議題＋一條界線＋開場問題", "One topic, boundary and opening question"))
            else -> listOf(
                recipe("寫低你想交出嘅成果", "Draft the result you want to produce", "景門", "想法仲未夠具體", "When the goal is still vague",
                    listOf("開一張便箋，標題寫「$g」。", "寫低「${goal.successCriteria}」要用甚麼證據確認，畫或寫出最小成果的樣子。", "列出目前欠的一項資料，留待核實；儲存這份草稿。"),
                    listOf("Open a note titled ‘$g’.", "Describe the smallest visible result and evidence for ‘${goal.successCriteria}’.", "Name one missing fact to verify and save the draft."), "一份最小成果草稿＋驗收證據", "A small draft and a way to check it"),
                recipe("用現有材料做一個小樣", "Make a small sample with what you have", "生門", "已清楚方向，想即刻試做", "When you are ready for a small trial",
                    listOf("為「$g」揀一個不用購買材料、今次時段可試的最小版本。", "用現有工具做一個例子／小樣；做唔到就記低第一個卡點，唔勉強花錢。", "對照「${goal.successCriteria}」，記低已做到甚麼、下一次只改一樣甚麼。"),
                    listOf("Choose the smallest version of ‘$g’ you can try with existing resources.", "Make one example or sample. If blocked, record the first obstacle without spending money.", "Compare with ‘${goal.successCriteria}’ and note one change for the next attempt."), "一個小樣或一個具體卡點記錄", "One sample or a specific recorded obstacle"),
                contact(g, "準備向一個人核實方向", "Prepare to check the idea with one person"))
        }
        return recipes.mapIndexed { i, (title, guide) ->
            val palace = chart.palaces.single { it.door == guide.focusDoor }
            base[i].copy(kind="NAV_${goal.purpose}_$i", title=title, guide=guide,
                locationText=palace.direction, provenance="tianji-goal-navigation/1.0",
                rationale="步驟依你的目的與限制編排；以${guide.focusDoor}作象意參考，方位取自本次實際盤。不是全盤吉凶判斷。")
        }
    }

    private fun recipe(title: String, en: String, door: String, fit: String, fitEn: String,
        zh: List<String>, stepsEn: List<String>, done: String, doneEn: String) =
        title to ActionGuide(titleEn=en,focusDoor=door,stepsZh=zh,stepsEn=stepsEn,doneZh=done,doneEn=doneEn,fitZh=fit,fitEn=fitEn)

    private fun contact(g: String, title: String, en: String) = recipe(title,en,"休門",
        "需要了解另一個人的想法或資料", "When you need someone else's information",
        listOf("揀一位你已認識、同「$g」有關而且適合聯絡的人；未有就先寫低所需角色。", "寫一句背景，再問一條具體問題，例如「我想確認＿＿，你方便畀一個建議嗎？」", "先存草稿。核對對方界線及合適時間，再自行決定是否發送；未回覆亦算完成準備。"),
        listOf("Identify an existing, appropriate contact for ‘$g’, or describe the role you need.", "Draft one sentence of context and one specific question asking whether they are willing to help.", "Save the draft. Check boundaries and timing before deciding whether to send; no reply is needed to complete preparation."),
        "一個合適聯絡對象／角色＋一條問題草稿", "One appropriate contact or role and a drafted question")

    fun palace(chart: Chart, action: ActionCandidate): Palace? = action.guide?.let { guide ->
        require(chart.chart_id == action.chartId && chart.status == "TRADITIONAL")
        chart.palaces.single { it.door == guide.focusDoor }
    }
    fun bearing(index: Int): Int = mapOf(1 to 0,8 to 45,3 to 90,4 to 135,9 to 180,2 to 225,7 to 270,6 to 315).getValue(index)
}
