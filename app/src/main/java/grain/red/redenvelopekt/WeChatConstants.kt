package grain.red.redenvelopekt

object WeChatConstants {
    val WE_CHAT_PACKAGE = "com.tencent.mm"

    /* 页面 */
     val activity_lucky_money =
        "$WE_CHAT_PACKAGE.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI" //微信红包弹框
    val activity_lucky_money_detail =
        "$WE_CHAT_PACKAGE.plugin.luckymoney.ui.LuckyMoneyDetailUI" //微信红包详情页

    var notification_special_word = "[微信红包]" //红包关键字

    /* 微信聊天列表页控件 */
    var chat_list_button_id = "com.tencent.mm:id/baj" //Item可点击控件id
    var chat_list_text_id = "com.tencent.mm:id/do8" //Item内容控件id，通过关键字判断

    /* 微信对话页控件 */
    var chat_view_pocket_click = "com.tencent.mm:id/aqk" // 红包框可点击控件id
    var chat_view_pocket_text_flag = "com.tencent.mm:id/t_" // 红包框左下角'微信红包'控件id
    var chat_view_pocket_opened_flag = "com.tencent.mm:id/sy" // 红包框中间文字'已领取'控件id

    /* 红包弹框控件*/
    var red_envelope_open_id = "com.tencent.mm:id/eh7" // 红包点开控件id
    var red_envelope_close_id = "com.tencent.mm:id/eh6" // 红包弹框关闭控件id


}