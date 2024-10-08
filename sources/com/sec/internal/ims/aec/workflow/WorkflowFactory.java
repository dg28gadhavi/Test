package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;

public class WorkflowFactory {
    private static volatile WorkflowFactory mInstance;
    private final SparseArray<IWorkflowImpl> mWorkflowArray = new SparseArray<>();
    private final SparseArray<HandlerThread> mWorkflowThreads = new SparseArray<>();

    private WorkflowFactory() {
    }

    public static WorkflowFactory getInstance() {
        if (mInstance == null) {
            synchronized (WorkflowFactory.class) {
                if (mInstance == null) {
                    mInstance = new WorkflowFactory();
                }
            }
        }
        return mInstance;
    }

    private synchronized IWorkflowImpl createWorkflow(Context context, String str, String str2, HandlerThread handlerThread, Handler handler) {
        IWorkflowImpl iWorkflowImpl;
        if ("ts43".equalsIgnoreCase(str2)) {
            iWorkflowImpl = str.equalsIgnoreCase("Dish_US") ? new WorkflowDSH(context, handlerThread.getLooper(), handler, WorkflowDSH.class.getSimpleName()) : str.equalsIgnoreCase("GenericIR92_US:CSpire") ? new WorkflowCSpire(context, handlerThread.getLooper(), handler, WorkflowDSH.class.getSimpleName()) : new WorkflowTS43(context, handlerThread.getLooper(), handler, WorkflowTS43.class.getSimpleName());
        } else {
            iWorkflowImpl = (!"nsds_eur".equalsIgnoreCase(str2) || (!str.equalsIgnoreCase("Telefonica_GB") && !str.equalsIgnoreCase("TelefonicaLAB_GB"))) ? null : new WorkflowO2U(context, handlerThread.getLooper(), handler, WorkflowO2U.class.getSimpleName());
        }
        return iWorkflowImpl;
    }

    public synchronized boolean createWorkflow(Context context, int i, String str, String str2, String str3, Handler handler) {
        HandlerThread handlerThread = new HandlerThread("Workflow" + i + str2);
        handlerThread.start();
        IWorkflowImpl createWorkflow = createWorkflow(context, str2, str3, handlerThread, handler);
        if (createWorkflow == null) {
            handlerThread.quit();
            return false;
        }
        this.mWorkflowArray.append(i, createWorkflow);
        if (this.mWorkflowThreads.get(i) != null) {
            this.mWorkflowThreads.get(i).quit();
        }
        this.mWorkflowThreads.append(i, handlerThread);
        createWorkflow.initWorkflow(i, str, str2);
        return true;
    }

    public synchronized IWorkflowImpl getWorkflow(int i) {
        return this.mWorkflowArray.get(i);
    }

    public synchronized SparseArray<IWorkflowImpl> getAllWorkflow() {
        return this.mWorkflowArray;
    }

    public synchronized void clearWorkflow(int i) {
        this.mWorkflowArray.remove(i);
        if (this.mWorkflowThreads.get(i) != null) {
            this.mWorkflowThreads.get(i).quit();
            this.mWorkflowThreads.remove(i);
        }
    }
}
