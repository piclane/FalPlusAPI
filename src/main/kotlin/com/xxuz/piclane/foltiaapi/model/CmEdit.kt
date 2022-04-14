package com.xxuz.piclane.foltiaapi.model

import kotlin.reflect.jvm.internal.impl.incremental.components.LookupTracker.DO_NOTHING

/**
 * CM カットに関する情報
 */
data class CmEdit(
    /** CM カット閾値 */
    val detectThreshold: DetectThreshold,

    /** CMカット MPEG2 */
    val tsRule: Rule,

    /** CMカット MP4 */
    val mp4Rule: Rule,
) {
    companion object {
        /**
         * CM カット閾値とメディア別 CM カットルールから CmEdit を生成します
         */
        fun of(detectThreshold: DetectThreshold, ruleSet: RuleSet) =
            CmEdit(detectThreshold, ruleSet.tsRule, ruleSet.mp4Rule)
    }

    /**
     * CM カット閾値
     */
    enum class DetectThreshold(
        val code: Int
    )  {
        /** オフ */
        OFF(0),

        /** 弱 */
        LOW(1),

        /** 中 */
        MEDIUM(2),

        /** 強 */
        HIGH(3);

        companion object {
            /**
             * コードから CmEdit.DetectThreshold を取得します
             */
            fun codeOf(code: Int?): DetectThreshold =
                if(code == null)
                    OFF
                else
                    values().find { it.code == code } ?: OFF
        }
    }

    /**
     * メディア別 CM カットルール
     */
    data class RuleSet(
        /** CMカット MPEG2 */
        val tsRule: Rule,

        /** CMカット MP4 */
        val mp4Rule: Rule,
    ) {
        companion object {
            /** 全メディアで編集しない */
            val DO_NOTHING = RuleSet(Rule.DO_NOTHING, Rule.DO_NOTHING)

            /**
             * コードから CmEdit.RuleSet を取得します
             */
            fun codeOf(code: Int?): RuleSet =
                if(code == null)
                    DO_NOTHING
                else
                    RuleSet(tsCodeOf(code), mp4CodeOf(code))

            /**
             * コードから TS 用の CmEdit.Rule を取得します
             */
            private fun tsCodeOf(code: Int): Rule {
                val tsCode = code % 10
                return Rule.values().find { it.code == tsCode } ?: Rule.DO_NOTHING
            }

            /**
             * コードから MP4 用の CmEdit.Rule を取得します
             */
            private fun mp4CodeOf(code: Int): Rule {
                val mp4Code = code / 10
                return Rule.values().find { it.code == mp4Code } ?: Rule.DO_NOTHING
            }
        }
    }

    /**
     * CM カットルール
     */
    enum class Rule(
        val code: Int
    )  {
        /** 編集しない */
        DO_NOTHING(0),

        /** 本編のみ (CMカット) */
        DELETE_CM(1),

        /** CMのみ (本編カット) */
        LEAVE_ONLY_CM(2),

        /** 本編+CM(同尺並べ替え) */
        SORT_CM(3),

        /** チャプタ追加 */
        ADD_CHAPTERS(4);
    }
}
