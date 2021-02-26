package grain.red.redenvelopekt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.PendingIntent
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import android.widget.Toast
import androidx.annotation.RequiresApi
import grain.red.redenvelopekt.WeChatConstants.chat_view_pocket_opened_flag
import grain.red.redenvelopekt.WeChatConstants.chat_view_pocket_text_flag
import grain.red.redenvelopekt.WeChatConstants.chat_view_pocket_click
import grain.red.redenvelopekt.WeChatConstants.red_envelope_open_id
import grain.red.redenvelopekt.WeChatConstants.chat_list_button_id
import grain.red.redenvelopekt.WeChatConstants.notification_special_word
import grain.red.redenvelopekt.WeChatConstants.activity_lucky_money
import grain.red.redenvelopekt.WeChatConstants.activity_lucky_money_detail
import grain.red.redenvelopekt.WeChatConstants.WE_CHAT_PACKAGE
import grain.red.redenvelopekt.WeChatConstants.chat_list_text_id
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.random.Random

class WeChatMonitorService : AccessibilityService() {

    var monitorPackageName = WE_CHAT_PACKAGE

    private val _tag = "WeChatRedEnvelopeService"
    private var isHasReceived: Boolean = false//true已经通知或聊天列表页面收到红包
    private var isHasClicked: Boolean = false//true点击红包弹出红包框
    private var isHasOpened: Boolean = false//true点击了拆开红包按钮
    private var currentClassName = "com.tencent.mm.ui.LauncherUI"
    var sleepSecond = 1

    override fun onCreate() {
        super.onCreate()
        val packageInfo = packageManager.getPackageInfo(monitorPackageName, 0)
        val versionName = packageInfo.versionName
        show(versionName)
        log(versionName)
    }

    override fun onInterrupt() {
        log("service close")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (monitorPackageName != event?.packageName ?: "") {
            return
        }
        currentClassName = event?.className.toString()
        log("catch event " + (event?.eventType ?: ""))
        when (event?.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                log("found in notification " + event.text)
                monitorNotification(event)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                log("界面改变$event")
                openRedEnvelope(event)
                quitEnvelope(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (rootInActiveWindow == null)
                    return
                log("内容改变")
                grabRedEnvelope()
                monitorChat()
            }
        }
    }

    /**
     * 监控通知是否有红包
     */
    private fun monitorNotification(event: AccessibilityEvent) {
        if (isHasReceived) return
        val texts = event.text
        log("检测到微信通知，文本为------------>$texts")
        if (texts.isEmpty())
            return
        if (texts.toString().contains(notification_special_word)) {
            log("monitorNotification:红包")
//            WakeUpUtil.wakeUpAndUnlock(applicationContext)
            val notification = event.parcelableData as Notification
            val pendingIntent = notification.contentIntent
            try {
                log("准备打开通知栏")
                pendingIntent.send()
                isHasReceived = true
            } catch (e: PendingIntent.CanceledException) {
                log("error:$e")
            }
        } else if (texts.toString().contains("clickNow")) {
            makeAClick()
        }
    }

    /**
     * 监控微信聊天列表页面是否有红包，经测试若聊天页面与通知同时开启聊天页面快
     */
    private fun monitorChat() {
        log("monitorChat")
        val chatSessions =
            rootInActiveWindow.findAccessibilityNodeInfosByViewId(chat_list_button_id)
                ?: return
        log("lists$chatSessions")
        for (session in chatSessions) {
            val textInSession =
                session.findAccessibilityNodeInfosByViewId(chat_list_text_id) ?: return
            for (textNode in textInSession) {
                log("文字-- ${textNode.text}")
                if (Objects.isNull(textNode.text)) {
                    continue
                }
                if (textNode.text.contains(notification_special_word)) {
                    log("found in chat list")
                    session.performAction(ACTION_CLICK)
                    isHasReceived = true
                    break
                }
            }
        }
    }

    /**
     * 聊天页面监控点击红包
     */
    private fun grabRedEnvelope() {
        log("grabRedEnvelope")

        val envelopes =
            rootInActiveWindow.findAccessibilityNodeInfosByViewId(chat_view_pocket_click) ?: return
        /* 发现红包点击进入领取红包页面 */

        GlobalScope.launch {
            val delayTime = 150L + sleepSecond * 100L
            delay(delayTime)
            for (envelope in envelopes.reversed()) {
                if (envelope.findAccessibilityNodeInfosByViewId(chat_view_pocket_opened_flag)
                        .isNotEmpty()
                ) {
                    continue
                }
                if (envelope.findAccessibilityNodeInfosByViewId(chat_view_pocket_text_flag)
                        .isEmpty()
                ) {
                    continue
                }
                log("发现红包：$envelope")
                envelope.performAction(ACTION_CLICK)
                isHasClicked = true
                break
            }
        }

        isHasReceived = false
    }

    /**
     * 拆开红包
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun openRedEnvelope(event: AccessibilityEvent) {
        if (event.className != activity_lucky_money) return
        GlobalScope.launch {
            val sleep = 300L + sleepSecond * 300L
            log("延时开红包:$sleep")
            delay(sleep)
            try {
                if (rootInActiveWindow == null) {
                    openRedEnvelopeNew()
                }else{
                    val envelopes =
                        getElementsById(rootInActiveWindow, red_envelope_open_id) ?: Collections.emptyList()
                    log("拆红包页面:$envelopes")
                    if (envelopes.isEmpty()) {
                        openRedEnvelopeNew()
                    } else {
                        log("拆红包页面:$envelopes")
                        /* 进入红包页面点击开按钮 */
                        for (envelope in envelopes.reversed()) {
                            GlobalScope.launch {
                                val sleep = Random.nextLong(500L, 1000L) * sleepSecond
                                log("delay open time:$sleep")
                                delay(sleep)
                                envelope.performAction(ACTION_CLICK)
                                isHasOpened = true
                                isHasClicked = false
                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun openRedEnvelopeNew() {
        log("Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT)
        if (!isHasClicked) return
        if (activity_lucky_money != currentClassName) return
        makeAClick()
        isHasOpened = true
        isHasClicked = false
    }

    /**
     * 创建一个点击
      */
    private fun makeAClick() {
        val path = Path()
        path.moveTo(730f, 1750f)
        val build = GestureDescription.Builder()
        val gestureDescription =
            build.addStroke(GestureDescription.StrokeDescription(path, 500, 100)).build()
        dispatchGesture(gestureDescription, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                show("onCompleted")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                show("onCancelled")
            }
        }, null)
    }

    /**
     * 退出红包详情页
     */
    private fun quitEnvelope(event: AccessibilityEvent) {
        log("quitEnvelope")
        if (event.className != activity_lucky_money_detail) return
        if (!isHasOpened) return
        GlobalScope.launch {
            val delayTime = 1000L * sleepSecond
            log("delay close time:$delayTime")
            if (delayTime != 11000L) {
                delay(delayTime)
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
        isHasOpened = false
    }


    private fun show(msg: String) {
        Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun log(msg: String) {
        Log.i(_tag, msg)
    }

    private fun getElementsById(
        nodeInfo: AccessibilityNodeInfo,
        id: String
    ): MutableList<AccessibilityNodeInfo>? {
        return nodeInfo.findAccessibilityNodeInfosByViewId(id)
    }
}