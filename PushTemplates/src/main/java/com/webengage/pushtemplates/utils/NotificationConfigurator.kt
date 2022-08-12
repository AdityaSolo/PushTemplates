package com.webengage.pushtemplates.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.models.TimerStyleData
import com.webengage.pushtemplates.R
import com.webengage.pushtemplates.receivers.PushIntentListener
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.WebEngage
import com.webengage.sdk.android.actions.render.PushNotificationData
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface

class NotificationConfigurator {

    fun getDefaultNotificationChannelID(
        context: Context, pushData: PushNotificationData
    ): String {
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        var channelId = WebEngage.get().webEngageConfig.defaultPushChannelConfiguration.notificationChannelId
        Log.d("PushTemplates", "default channel -> $channelId")
        if(notificationManagerCompat.getNotificationChannel(pushData.channelId) != null)
            channelId = pushData.channelId

        return channelId
    }

    fun getDefaultNotificationChannel(
        context: Context,
    ): NotificationChannelCompat {
        val channel =
            NotificationChannelCompat.Builder("Sales", NotificationCompat.PRIORITY_DEFAULT)
                .setShowBadge(true)
                .setDescription("Sales Notifications")
                .setName("Sales")
                .setLightsEnabled(true)
                .setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
                .setImportance(NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setVibrationEnabled(true)
                .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
        return channel
    }

    fun setClickIntent(context: Context, remoteView: RemoteViews, pushData: TimerStyleData) {
        val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
            context,
            pushData.pushNotification,
            pushData.pushNotification.primeCallToAction,
            false
        )
        remoteView.setOnClickPendingIntent(R.id.we_notification_container, clickIntent)
    }

    fun setDismissIntent(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyleData
    ) {
        val deleteIntent = PendingIntentFactory.constructPushDeletePendingIntent(
            context,
            pushData.pushNotification
        )
        mBuilder.setDeleteIntent(deleteIntent)
    }

    fun setClickIntent(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyleData
    ) {
        val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
            context,
            pushData.pushNotification,
            pushData.pushNotification.primeCallToAction,
            true
        )
        mBuilder.setContentIntent(clickIntent)
    }

    fun setAdaptiveCTAs(context: Context, remoteViews: RemoteViews, pushData: TimerStyleData) {
        val pendingIntent = getNotificationDismissPendingIntent(context, pushData.pushNotification, true)
        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.pushNotification.callToActions != null && pushData.pushNotification.callToActions.size == 2) {
            if (pushData.pushNotification.callToActions[1] != null) {
                remoteViews.setViewVisibility(R.id.action1_adaptive, View.VISIBLE)
                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData.pushNotification,
                    pushData.pushNotification.callToActions[1],
                    true
                )
                remoteViews.setTextViewText(
                    R.id.action1_adaptive,
                    pushData.pushNotification.callToActions[1].text
                )

                remoteViews.setOnClickPendingIntent(R.id.action1_adaptive, clickIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action1_adaptive, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action1_adaptive, Constants.DISMISS_CTA)
                remoteViews.setOnClickPendingIntent(R.id.action1_adaptive, pendingIntent)
            }
            if (pushData.pushNotification.callToActions.size == 3) {
                remoteViews.setViewVisibility(R.id.action2_adaptive, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.action3_adaptive, View.VISIBLE)
                remoteViews.setTextViewText(
                    R.id.action2_adaptive,
                    pushData.pushNotification.callToActions[2].text
                )
                remoteViews.setTextViewText(R.id.action3_adaptive, Constants.DISMISS_CTA)

                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData.pushNotification,
                    pushData.pushNotification.callToActions[2],
                    true
                )
                remoteViews.setOnClickPendingIntent(R.id.action2_adaptive, clickIntent)
                remoteViews.setOnClickPendingIntent(R.id.action3_adaptive, pendingIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action2_adaptive, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action2_adaptive, Constants.DISMISS_CTA)
                remoteViews.setOnClickPendingIntent(R.id.action2_adaptive, pendingIntent)

            }
        } else {
            remoteViews.setViewVisibility(R.id.action1_adaptive, View.VISIBLE)
            remoteViews.setTextViewText(R.id.action1_adaptive, Constants.DISMISS_CTA)
            remoteViews.setOnClickPendingIntent(R.id.action1_adaptive, pendingIntent)
        }
    }


    /**
     * Dismiss button will be set explicitly at the end position.
     * **/
    fun setNativeCTAs(context: Context, remoteViews: RemoteViews, pushData: TimerStyleData) {
        var dismissSet = false
        val pendingIntent = getNotificationDismissPendingIntent(context, pushData.pushNotification, true)

        remoteViews.setViewVisibility(R.id.actions_container, View.VISIBLE)

        if (pushData.pushNotification.callToActions != null && pushData.pushNotification.callToActions.size > 1) {
            if (pushData.pushNotification.callToActions[1] != null) {
                remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData.pushNotification,
                    pushData.pushNotification.callToActions[1],
                    true
                )
                remoteViews.setTextViewText(
                    R.id.action1_native,
                    pushData.pushNotification.callToActions[1].text
                )

                remoteViews.setOnClickPendingIntent(R.id.action1_native, clickIntent)
            } else {
                remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
                remoteViews.setTextViewText(R.id.action1_native, Constants.DISMISS_CTA)
                dismissSet = true
                remoteViews.setOnClickPendingIntent(R.id.action1_native, pendingIntent)
            }
            if (pushData.pushNotification.callToActions.size > 2) {
                remoteViews.setViewVisibility(R.id.action2_native, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.action3_native, View.VISIBLE)
                remoteViews.setTextViewText(
                    R.id.action2_native,
                    pushData.pushNotification.callToActions[2].text
                )
                remoteViews.setTextViewText(R.id.action3_native, Constants.DISMISS_CTA)

                val clickIntent = PendingIntentFactory.constructPushClickPendingIntent(
                    context,
                    pushData.pushNotification,
                    pushData.pushNotification.callToActions[2],
                    true
                )
                dismissSet = true

                remoteViews.setOnClickPendingIntent(R.id.action2_native, clickIntent)
                remoteViews.setOnClickPendingIntent(R.id.action3_native, pendingIntent)
            } else {
                if (!dismissSet) {

                    remoteViews.setViewVisibility(R.id.action2_native, View.VISIBLE)
                    remoteViews.setTextViewText(R.id.action2_native, Constants.DISMISS_CTA)

                    dismissSet = true
                    remoteViews.setOnClickPendingIntent(R.id.action2_native, pendingIntent)
                }
            }
        } else {
            remoteViews.setViewVisibility(R.id.action1_native, View.VISIBLE)
            remoteViews.setTextViewText(R.id.action1_native, Constants.DISMISS_CTA)
            remoteViews.setOnClickPendingIntent(R.id.action1_native, pendingIntent)
        }
    }


    fun setCTAList(context: Context, remoteViews: RemoteViews, pushData: TimerStyleData) {
        remoteViews.setViewVisibility(R.id.we_notification_bottom_margin, View.GONE)
        if (pushData.pushNotification.backgroundColor != Color.parseColor("#00000000"))
            setNativeCTAs(context, remoteViews, pushData)
        else
            setAdaptiveCTAs(context, remoteViews, pushData)
    }

    fun configureRemoteView(
        context: Context,
        remoteView: RemoteViews,
        pushData: TimerStyleData,
        whenTime: Long
    ) {
        remoteView.setInt(
            R.id.we_notification_container,
            "setBackgroundColor",
            pushData.pushNotification.backgroundColor
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S) {
            remoteView.setViewVisibility(R.id.push_base_container, View.GONE)
            remoteView.setViewPadding(R.id.we_notification, 0, 0, 0, 0)
        } else {
            remoteView.setViewVisibility(R.id.push_base_container, View.VISIBLE)
            remoteView.setImageViewResource(R.id.small_icon, pushData.pushNotification.smallIcon)
            remoteView.setTextViewText(R.id.app_name, pushData.pushNotification.appName)
            if (!TextUtils.isEmpty(pushData.pushNotification.contentSummary))
                remoteView.setTextViewText(
                    R.id.custom_summary,
                    WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentSummary)
                )
            else
                remoteView.setViewVisibility(R.id.custom_summary, View.GONE)
            val dateFormat = DateFormat.getTimeFormat(context)
            val time = dateFormat.format(whenTime)
            remoteView.setTextViewText(
                com.webengage.sdk.android.R.id.custom_notification_time,
                time
            )
            remoteView.setTextViewText(R.id.app_name_native, pushData.pushNotification.appName)
            if (!TextUtils.isEmpty(pushData.pushNotification.contentSummary))
                remoteView.setTextViewText(
                    R.id.custom_summary_native,
                    WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentSummary)
                )
            else
                remoteView.setViewVisibility(R.id.custom_summary_native, View.GONE)
            remoteView.setTextViewText(
                com.webengage.sdk.android.R.id.custom_notification_time_native,
                time
            )

            if (pushData.pushNotification.backgroundColor != Color.parseColor("#00000000")) {
                //No Background Color Set

                remoteView.setViewVisibility(R.id.app_name, View.GONE)
                remoteView.setViewVisibility(R.id.custom_notification_time, View.GONE)
                remoteView.setViewVisibility(R.id.custom_summary, View.GONE)
            } else {
                //Background Color Set
                remoteView.setViewVisibility(R.id.app_name_native, View.GONE)
                remoteView.setViewVisibility(R.id.custom_notification_time_native, View.GONE)
                remoteView.setViewVisibility(R.id.custom_summary_native, View.GONE)
            }

        }
    }

    fun setNotificationTitle(context: Context, pushData: TimerStyleData, remoteViews: RemoteViews) {
        remoteViews.setTextViewText(
            R.id.we_notification_title,
            WEHtmlParserInterface().fromHtml(pushData.pushNotification.title)
        )
        remoteViews.setTextViewText(
            R.id.we_notification_title_native,
            WEHtmlParserInterface().fromHtml(pushData.pushNotification.title)
        )

        if (pushData.pushNotification.backgroundColor == Color.parseColor("#00000000")) {
            remoteViews.setViewVisibility(R.id.we_notification_title, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.we_notification_title_native, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_title, View.GONE)
            remoteViews.setViewVisibility(R.id.we_notification_title_native, View.VISIBLE)
        }
    }

    fun setNotificationDescription(
        context: Context,
        pushData: TimerStyleData,
        remoteViews: RemoteViews
    ) {
        remoteViews.setTextViewText(
            R.id.we_notification_description,
            WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentText)
        )
        remoteViews.setTextViewText(
            R.id.we_notification_description_native,
            WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentText)
        )

        if (pushData.pushNotification.backgroundColor == Color.parseColor("#00000000")) {
            remoteViews.setViewVisibility(R.id.we_notification_description, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.we_notification_description_native, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.we_notification_description, View.GONE)
            remoteViews.setViewVisibility(R.id.we_notification_description_native, View.VISIBLE)
        }
    }

    fun setNotificationConfiguration(
        context: Context,
        mBuilder: NotificationCompat.Builder,
        pushData: TimerStyleData,
        whenTime: Long
    ) {
        mBuilder.setAutoCancel(true)
        mBuilder.setOngoing(pushData.pushNotification.isSticky)
        mBuilder.setSmallIcon(pushData.pushNotification.smallIcon)
        mBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        mBuilder.setContentTitle(WEHtmlParserInterface().fromHtml(pushData.pushNotification.title))
        mBuilder.setContentText(WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentText))
        if (!TextUtils.isEmpty(pushData.pushNotification.contentSummary))
            mBuilder.setSubText(WEHtmlParserInterface().fromHtml(pushData.pushNotification.contentSummary))
        mBuilder.setWhen(whenTime)
    }

     fun getNotificationDismissPendingIntent(context: Context, pushData: PushNotificationData, logDismiss : Boolean): PendingIntent {
        val intent = Intent(context, PushIntentListener::class.java)
        intent.action = Constants.DELETE_ACTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier = (pushData.variationId+"_"+logDismiss)
        }
        intent.addCategory(context.packageName)
        intent.putExtra(Constants.PAYLOAD, pushData.pushPayloadJSON.toString())
        intent.putExtra(Constants.LOG_DISMISS,logDismiss)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                (pushData.variationId+"_"+logDismiss).hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                (pushData.variationId+"_"+logDismiss).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return pendingIntent
    }

}