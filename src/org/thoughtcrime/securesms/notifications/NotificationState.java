package org.thoughtcrime.securesms.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.whispersystems.textsecure.crypto.MasterSecret;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NotificationState {

  private final LinkedList<NotificationItem> notifications = new LinkedList<NotificationItem>();
  private final Set<Long>                    threads       = new HashSet<Long>();

  private int notificationCount = 0;

  public void addNotification(NotificationItem item) {
    notifications.addFirst(item);
    threads.add(item.getThreadId());
    notificationCount++;
  }

  public boolean hasMultipleThreads() {
    return threads.size() > 1;
  }

  public int getMessageCount() {
    return notificationCount;
  }

  public List<NotificationItem> getNotifications() {
    return notifications;
  }

  public Bitmap getContactPhoto() {
    return notifications.get(0).getIndividualRecipient().getContactPhoto();
  }

  public PendingIntent getMarkAsReadIntent(Context context, MasterSecret masterSecret) {
    long[] threadArray = new long[threads.size()];
    int index          = 0;

    for (long thread : threads) {
      Log.w("NotificationState", "Added thread: " + thread);
      threadArray[index++] = thread;
    }

    Intent intent = new Intent(MarkReadReceiver.CLEAR_ACTION);
    intent.putExtra("thread_ids", threadArray);
    intent.putExtra("master_secret", masterSecret);
    intent.setPackage(context.getPackageName());

    // XXX : This is an Android bug.  If we don't pull off the extra
    // once before handing off the PendingIntent, the array will be
    // truncated to one element when the PendingIntent fires.  Thanks guys!
    Log.w("NotificationState", "Pending array off intent length: " +
        intent.getLongArrayExtra("thread_ids").length);

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

    /**
     * Returns the callback PendingIntent for the message sender if only one thread exists.
     * @param context the context from which to launch the intent
     * @return a PendingIntent to launch a phone call activity, or null if there is more than one
     * thread
     */
    public PendingIntent getCallbackIntent(Context context) {
        if (threads.size() > 1) {
            Log.v("NotificationState", "More than one thread");
            return null;
        }

        // Create url string of the from "tel:1234567890"
        String uri = "tel:"+notifications.get(0).getIndividualRecipient().getNumber().replaceAll("[^0-9|\\+]", "");
        Log.v("NotificationState", "New URI: "+uri);

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
        Log.v("NotificationState", "Parsed URI");

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
