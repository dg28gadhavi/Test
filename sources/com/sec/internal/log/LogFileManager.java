package com.sec.internal.log;

import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import com.sec.internal.helper.FileUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;

class LogFileManager {
    private static String LOG_TAG = "LogFileManager";
    private String mFullPath;
    private int mMaxCount;
    private int mMaxSize;
    private MeteredWriter mMeter;
    private Path[] mPaths;

    LogFileManager(String str, int i, int i2) {
        if (TextUtils.isEmpty(str) || i < 0 || i2 < 1) {
            throw new IllegalArgumentException();
        }
        this.mFullPath = str;
        this.mMaxSize = i;
        this.mMaxCount = i2;
    }

    /* access modifiers changed from: package-private */
    public void init() {
        this.mPaths = new Path[this.mMaxCount];
        for (int i = 0; i < this.mMaxCount; i++) {
            this.mPaths[i] = Paths.get(String.format(Locale.US, "%s.%d", new Object[]{this.mFullPath, Integer.valueOf(i)}), new String[0]);
        }
        cleanupLegacyLogs();
        try {
            open(this.mPaths[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanupLegacyLogs() {
        if (!isLogGroup(this.mPaths[0].getParent())) {
            FileUtils.deleteDirectory(this.mPaths[0].getParent());
        }
    }

    private boolean isLogGroup(Path path) {
        try {
            if (Os.stat(path.toAbsolutePath().toString()).st_gid == 1007) {
                return true;
            }
            return false;
        } catch (ErrnoException e) {
            String str = LOG_TAG;
            IMSLog.e(str, "isLogGroup exception : " + e.getMessage());
            return false;
        }
    }

    private void setPermission(Path path) {
        try {
            Os.chown(path.toAbsolutePath().toString(), 1000, 1007);
            Os.chmod(path.toAbsolutePath().toString(), 488);
        } catch (ErrnoException e) {
            String str = LOG_TAG;
            IMSLog.e(str, "setPermission exception : " + e.getMessage());
        }
    }

    private void open(Path path) throws IOException {
        long j;
        StandardOpenOption standardOpenOption = StandardOpenOption.WRITE;
        if (Files.exists(path, new LinkOption[0])) {
            long size = Files.size(path);
            standardOpenOption = StandardOpenOption.APPEND;
            j = size;
        } else {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            setPermission(path.getParent());
            Files.createFile(path, new FileAttribute[0]);
            setPermission(path);
            j = 0;
        }
        this.mMeter = new MeteredWriter(Files.newBufferedWriter(path, new OpenOption[]{standardOpenOption}), j);
    }

    private synchronized void rotate() throws IOException {
        for (int i = this.mMaxCount - 2; i >= 0; i--) {
            if (Files.exists(this.mPaths[i], new LinkOption[0])) {
                Path[] pathArr = this.mPaths;
                Files.move(pathArr[i], pathArr[i + 1], new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            }
        }
        open(this.mPaths[0]);
    }

    /* access modifiers changed from: package-private */
    public synchronized void write(String str) {
        boolean z;
        try {
            if (this.mMeter == null || Files.notExists(this.mPaths[0], new LinkOption[0])) {
                open(this.mPaths[0]);
            }
            this.mMeter.write(str);
            z = true;
        } catch (IOException unused) {
            z = false;
        }
        if (!z) {
            try {
                open(this.mPaths[0]);
                this.mMeter.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.mMeter.written > ((long) this.mMaxSize)) {
            rotate();
        }
        return;
    }

    private static class MeteredWriter {
        final Writer writer;
        long written;

        MeteredWriter(Writer writer2, long j) {
            this.writer = writer2;
            this.written = j;
        }

        public void write(String str) throws IOException {
            this.writer.write(str);
            this.writer.flush();
            this.written += (long) str.length();
        }
    }
}
