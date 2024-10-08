package com.sec.internal.helper;

import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IndentingPrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleEventLog {
    private final int LOG_FILE_RECORD_LIMIT;
    final Path LOG_PATH;
    private final String LOG_TAG;
    private final String NAME;
    ExecutorService mFileIOExecutor;
    ScheduledFuture<?> mFlushFuture;
    final List<String> mLogBuffer;
    ScheduledExecutorService mPeriodicExecutor;
    ScheduledFuture<?> mResizeFuture;

    public SimpleEventLog(Context context, String str, int i) {
        this.LOG_TAG = "SimpleEventLog";
        this.mLogBuffer = new ArrayList();
        this.NAME = str;
        this.LOG_FILE_RECORD_LIMIT = i;
        String absolutePath = context.getFilesDir().getAbsolutePath();
        this.LOG_PATH = Paths.get(absolutePath, new String[]{str + ".log"});
        this.mFileIOExecutor = Executors.newSingleThreadExecutor();
        this.mPeriodicExecutor = Executors.newSingleThreadScheduledExecutor();
        add("> Created (pid: " + Binder.getCallingPid() + ", binary: " + Build.VERSION.INCREMENTAL + ")");
    }

    public SimpleEventLog(Context context, int i, String str, int i2) {
        this(context, String.format(Locale.US, "%s_slot%d", new Object[]{str, Integer.valueOf(i)}), i2);
    }

    public void add(String str) {
        synchronized (this.mLogBuffer) {
            List<String> list = this.mLogBuffer;
            list.add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date()) + "   " + str);
        }
        try {
            ScheduledFuture<?> scheduledFuture = this.mFlushFuture;
            if (scheduledFuture == null || scheduledFuture.isDone()) {
                this.mFlushFuture = this.mPeriodicExecutor.schedule(new SimpleEventLog$$ExternalSyntheticLambda1(this), 1, TimeUnit.MINUTES);
            }
            ScheduledFuture<?> scheduledFuture2 = this.mResizeFuture;
            if (scheduledFuture2 == null || scheduledFuture2.isDone()) {
                this.mResizeFuture = this.mPeriodicExecutor.schedule(new SimpleEventLog$$ExternalSyntheticLambda2(this), 30, TimeUnit.MINUTES);
            }
        } catch (OutOfMemoryError | RejectedExecutionException e) {
            IMSLog.e("SimpleEventLog", this.NAME + ": Failed to schedule periodic events. " + e);
        }
    }

    public void add(int i, String str) {
        add("slot[" + i + "]: " + str);
    }

    public void logAndAdd(String str) {
        Log.i(this.NAME, str);
        add(str);
    }

    public void debugLogAndAdd(String str, String str2) {
        Log.d(str, str2);
        add(str + ": " + str2);
    }

    public void infoLogAndAdd(String str, String str2) {
        Log.i(str, str2);
        add(str + ": " + str2);
    }

    public void logAndAdd(int i, String str) {
        logAndAdd("slot[" + i + "]: " + str);
    }

    public void logAndAdd(int i, IRegisterTask iRegisterTask, String str) {
        logAndAdd("slot[" + i + "]: [" + iRegisterTask.getProfile().getName() + "|" + iRegisterTask.getState() + "] " + str);
    }

    public void dump(IndentingPrintWriter indentingPrintWriter) {
        flushForDump();
        indentingPrintWriter.println("\nDump of " + this.NAME + ":");
        indentingPrintWriter.increaseIndent();
        try {
            for (String println : Files.readAllLines(this.LOG_PATH)) {
                indentingPrintWriter.println(println);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        indentingPrintWriter.decreaseIndent();
    }

    public void dump() {
        flushForDump();
        String str = this.NAME;
        IMSLog.dump(str, "EventLog(" + this.NAME + "):");
        IMSLog.increaseIndent(this.NAME);
        try {
            for (String dump : Files.readAllLines(this.LOG_PATH)) {
                IMSLog.dump(this.NAME, dump);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        IMSLog.decreaseIndent(this.NAME);
    }

    /* access modifiers changed from: package-private */
    public CompletableFuture<Void> flush() {
        synchronized (this.mLogBuffer) {
            if (this.mLogBuffer.isEmpty()) {
                CompletableFuture<Void> completedFuture = CompletableFuture.completedFuture((Object) null);
                return completedFuture;
            }
            ArrayList arrayList = new ArrayList(this.mLogBuffer);
            CompletableFuture completableFuture = new CompletableFuture();
            this.mFileIOExecutor.submit(new SimpleEventLog$$ExternalSyntheticLambda3(this, completableFuture, arrayList));
            return completableFuture.thenAccept(new SimpleEventLog$$ExternalSyntheticLambda4(this));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$flush$0(CompletableFuture completableFuture, List list) throws Exception {
        return Boolean.valueOf(completableFuture.complete(Integer.valueOf(writeAll(list, StandardOpenOption.APPEND))));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$flush$1(Integer num) {
        synchronized (this.mLogBuffer) {
            if (num.intValue() <= this.mLogBuffer.size()) {
                this.mLogBuffer.subList(0, num.intValue()).clear();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flushForDump() {
        try {
            flush().get(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logAndAdd(this.NAME + ": flush failed by " + e);
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void resize() {
        try {
            this.mFileIOExecutor.submit(new SimpleEventLog$$ExternalSyntheticLambda0(this));
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$resize$2() {
        if (Files.exists(this.LOG_PATH, new LinkOption[0])) {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                List<String> readAllLines = Files.readAllLines(this.LOG_PATH);
                int size = readAllLines.size();
                Log.i("SimpleEventLog", this.NAME + " Read written lines: " + size + "(" + (System.currentTimeMillis() - currentTimeMillis) + " ms)");
                int i = size - this.LOG_FILE_RECORD_LIMIT;
                if (i > 0) {
                    writeAll(readAllLines.subList(i, size), StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int writeAll(List<String> list, OpenOption... openOptionArr) {
        BufferedWriter newBufferedWriter;
        List list2 = (List) Stream.of(new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE}).collect(Collectors.toList());
        list2.addAll(Arrays.asList(openOptionArr));
        int i = 0;
        try {
            newBufferedWriter = Files.newBufferedWriter(this.LOG_PATH, (OpenOption[]) list2.toArray(new OpenOption[0]));
            long currentTimeMillis = System.currentTimeMillis();
            for (String next : list) {
                if (!TextUtils.isEmpty(next)) {
                    newBufferedWriter.write(next);
                    newBufferedWriter.newLine();
                }
                i++;
            }
            Log.d("SimpleEventLog", this.NAME + " File writing done: " + list.size() + "(" + (System.currentTimeMillis() - currentTimeMillis) + " ms)");
            if (newBufferedWriter != null) {
                newBufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return i;
        throw th;
    }
}
