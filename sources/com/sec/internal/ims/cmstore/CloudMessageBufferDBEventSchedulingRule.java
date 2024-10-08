package com.sec.internal.ims.cmstore;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;

public class CloudMessageBufferDBEventSchedulingRule {
    private static final String TAG = "CloudMessageBufferDBEventSchedulingRule";

    private void onImpossibleCombination() {
        Log.d(TAG, "onImpossibleCombination");
    }

    private void onUnprocessedCombination() {
        Log.d(TAG, "onUnprocessedCombination");
    }

    private void onActionCanceledOutEvents() {
        Log.d(TAG, "onActionCanceledOutEvents");
    }

    public ParamSyncFlagsSet getSetFlagsForMsgOperation(int i, long j, CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag) {
        CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.Done;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.None;
        ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(directionFlag2, actionStatusFlag2);
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[msgOperationFlag.ordinal()]) {
            case 1:
                handleReceivingOperationForFlags(actionStatusFlag, paramSyncFlagsSet);
                break;
            case 2:
            case 3:
                if ((!actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) && !actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) || !directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    if (!handleWhenSendFailOrReceived(actionStatusFlag)) {
                        if (!actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                            if (!actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri)) {
                                if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad) && directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Downloading)) {
                                    paramSyncFlagsSet.mIsChanged = true;
                                    break;
                                }
                            } else {
                                paramSyncFlagsSet.mIsChanged = false;
                                break;
                            }
                        } else {
                            paramSyncFlagsSet.mIsChanged = false;
                            break;
                        }
                    } else {
                        onUnprocessedCombination();
                        break;
                    }
                } else {
                    paramSyncFlagsSet.mAction = actionStatusFlag;
                    paramSyncFlagsSet.mDirection = directionFlag;
                    break;
                }
                break;
            case 4:
            case 5:
                if (!handleWhenSendFailOrReceived(actionStatusFlag)) {
                    if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                        paramSyncFlagsSet.mIsChanged = false;
                        break;
                    }
                } else {
                    onUnprocessedCombination();
                    break;
                }
                break;
            case 6:
                handleReadOperationForFlags(directionFlag, actionStatusFlag, paramSyncFlagsSet);
                break;
            case 7:
                handleDeleteOperationForFlags(directionFlag, actionStatusFlag, paramSyncFlagsSet);
                break;
            case 8:
                if ((actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) && handleActionStatusFlagInsertWhenDeleteOrCancel(directionFlag)) || (actionStatusFlag.equals(actionStatusFlag2) && directionFlag.equals(directionFlag2))) {
                    paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Cancel;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    break;
                }
            case 9:
                if (actionStatusFlag.equals(actionStatusFlag2)) {
                    paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Starred;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    break;
                }
                break;
            case 10:
                if (actionStatusFlag.equals(actionStatusFlag2)) {
                    paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.UnStarred;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    break;
                }
                break;
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + i + ", bufferId: " + j + ", getSetFlagsForMsgOperation, origDir: " + directionFlag + " origAction: " + actionStatusFlag + " msgOperation: " + msgOperationFlag + ", sync flag result :" + paramSyncFlagsSet.toString());
        return paramSyncFlagsSet;
    }

    private void handleDeleteOperationForFlags(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        if (!actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) && !actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Cancel)) {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            if (actionStatusFlag.equals(actionStatusFlag2)) {
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    return;
                }
                CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                if (directionFlag.equals(directionFlag2)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = directionFlag2;
                    paramSyncFlagsSet.mIsChanged = false;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                if (handleActionStatusFlagInsertWhenDeleteOrCancel(directionFlag)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                paramSyncFlagsSet.mIsChanged = false;
            }
        } else if (handleActionStatusFlagUpdateWhenDelete(directionFlag)) {
            paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
        }
    }

    private void handleReadOperationForFlags(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
        if (!actionStatusFlag.equals(actionStatusFlag2)) {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            if (actionStatusFlag.equals(actionStatusFlag3)) {
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
                    paramSyncFlagsSet.mIsChanged = false;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag3;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    paramSyncFlagsSet.mIsChanged = false;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    paramSyncFlagsSet.mIsChanged = false;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert)) {
                if (handleActionStatusFlagInsertWhenRead(directionFlag)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                paramSyncFlagsSet.mIsChanged = false;
            }
        } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
            paramSyncFlagsSet.mAction = actionStatusFlag2;
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
        } else {
            CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
            if (directionFlag.equals(directionFlag2)) {
                paramSyncFlagsSet.mAction = actionStatusFlag2;
                paramSyncFlagsSet.mDirection = directionFlag2;
                paramSyncFlagsSet.mIsChanged = false;
            }
        }
    }

    private void handleReceivingOperationForFlags(CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
            paramSyncFlagsSet.mIsChanged = false;
        }
    }

    private boolean handleWhenSendFailOrReceived(CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag) {
        return actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None);
    }

    private boolean handleActionStatusFlagInsertWhenRead(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }

    private boolean handleActionStatusFlagUpdateWhenDelete(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail);
    }

    private boolean handleActionStatusFlagInsertWhenDeleteOrCancel(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice);
    }

    private boolean handleActionStatusFlagDeleteWhenDelete(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice);
    }

    public ParamSyncFlagsSet getSetFlagsForCldOperationForCms(int i, long j, CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2) {
        String str = TAG;
        Log.d(str, "getSetFlagsForCldOperationForCms dbIndex: " + i + ", bufferId: " + j + ", origDir: " + directionFlag + " origAction: " + actionStatusFlag + " cldAction: " + actionStatusFlag2);
        ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
        if (!actionStatusFlag2.equals(actionStatusFlag3) || !actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || !directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
            return getSetFlagsForCldOperation(i, j, directionFlag, actionStatusFlag, actionStatusFlag2);
        }
        paramSyncFlagsSet.mAction = actionStatusFlag3;
        paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
        return paramSyncFlagsSet;
    }

    public ParamSyncFlagsSet getSetFlagsForCldOperation(int i, long j, CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2) {
        CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.Done;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.None;
        ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(directionFlag2, actionStatusFlag3);
        int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[actionStatusFlag2.ordinal()];
        if (i2 == 1) {
            handleUpdateOperationForFlags(directionFlag, actionStatusFlag, paramSyncFlagsSet);
        } else if (i2 == 2) {
            handleInsertOperationForFlags(directionFlag, actionStatusFlag, paramSyncFlagsSet);
        } else if (i2 == 3) {
            handleDeleteCloudOperationForFlags(directionFlag, actionStatusFlag, paramSyncFlagsSet);
        } else if (i2 == 4) {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag4 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            if (actionStatusFlag.equals(actionStatusFlag4)) {
                paramSyncFlagsSet.mIsChanged = false;
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                    paramSyncFlagsSet.mIsChanged = true;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                    paramSyncFlagsSet.mAction = actionStatusFlag4;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad) || (actionStatusFlag.equals(actionStatusFlag3) && directionFlag.equals(directionFlag2))) {
                paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Cancel;
                paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
            }
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + i + ", bufferId: " + j + ", getSetFlagsForCldOperation, origDir: " + directionFlag + " origAction: " + actionStatusFlag + " cldAction: " + actionStatusFlag2 + ", sync flag result :" + paramSyncFlagsSet.toString());
        return paramSyncFlagsSet;
    }

    private void handleDeleteCloudOperationForFlags(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        if (!actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) && !actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload)) {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            if (actionStatusFlag.equals(actionStatusFlag2)) {
                if (isChangedUpdateDirection(directionFlag)) {
                    paramSyncFlagsSet.mIsChanged = false;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri)) {
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                    paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                    paramSyncFlagsSet.mIsChanged = true;
                    paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    onImpossibleCombination();
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                    paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                    paramSyncFlagsSet.mAction = actionStatusFlag2;
                }
            } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                paramSyncFlagsSet.mIsChanged = false;
            }
        } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent)) {
            paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
        } else {
            if (!directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) && !directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) && !directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                if (!directionFlag.equals(directionFlag2)) {
                    if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                        paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                        paramSyncFlagsSet.mDirection = directionFlag2;
                        return;
                    }
                    return;
                }
            }
            paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
        }
    }

    private void handleInsertOperationForFlags(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
        if (actionStatusFlag.equals(actionStatusFlag2) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload)) {
            paramSyncFlagsSet.mIsChanged = false;
            if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                paramSyncFlagsSet.mIsChanged = true;
                paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                paramSyncFlagsSet.mAction = actionStatusFlag2;
                return;
            }
            return;
        }
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
        if (actionStatusFlag.equals(actionStatusFlag3)) {
            paramSyncFlagsSet.mIsChanged = false;
            if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                paramSyncFlagsSet.mIsChanged = true;
                paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                paramSyncFlagsSet.mAction = actionStatusFlag3;
            }
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri)) {
            if (!directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) && !directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                onImpossibleCombination();
                paramSyncFlagsSet.mIsChanged = false;
            }
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
            onImpossibleCombination();
            paramSyncFlagsSet.mIsChanged = false;
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
            paramSyncFlagsSet.mIsChanged = false;
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad)) {
            paramSyncFlagsSet.mIsChanged = false;
        }
    }

    private void handleUpdateOperationForFlags(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
        if (!actionStatusFlag.equals(actionStatusFlag2) && !actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload)) {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            if (!actionStatusFlag.equals(actionStatusFlag3)) {
                CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag4 = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
                if (actionStatusFlag.equals(actionStatusFlag4) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce)) {
                    if (isChangedDirection(directionFlag)) {
                        paramSyncFlagsSet.mIsChanged = false;
                    } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                        paramSyncFlagsSet.mIsChanged = true;
                    } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
                        paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                        paramSyncFlagsSet.mAction = actionStatusFlag4;
                    }
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None)) {
                    if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                        paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
                        paramSyncFlagsSet.mAction = actionStatusFlag2;
                    }
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                    paramSyncFlagsSet.mIsChanged = false;
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad)) {
                    paramSyncFlagsSet.mIsChanged = false;
                }
            } else if (isChangedDeleteDirection(directionFlag)) {
                paramSyncFlagsSet.mIsChanged = false;
            } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                paramSyncFlagsSet.mAction = actionStatusFlag3;
            }
        } else if (isChangedUpdateDirection(directionFlag)) {
            paramSyncFlagsSet.mIsChanged = false;
        } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail)) {
            paramSyncFlagsSet.mIsChanged = true;
            paramSyncFlagsSet.mAction = actionStatusFlag2;
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006f, code lost:
        if (r3 != 7) goto L_0x00d7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet getSetFlagsForMsgResponse(int r6, long r7, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag r9, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag r10, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag r11) {
        /*
            r5 = this;
            boolean r0 = r11.equals(r10)
            r1 = 0
            if (r0 == 0) goto L_0x003a
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            boolean r0 = r9.equals(r0)
            if (r0 == 0) goto L_0x003a
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri
            boolean r5 = r10.equals(r5)
            if (r5 != 0) goto L_0x0032
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri
            boolean r5 = r10.equals(r5)
            if (r5 != 0) goto L_0x0032
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce
            boolean r5 = r10.equals(r5)
            if (r5 == 0) goto L_0x0028
            goto L_0x0032
        L_0x0028:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r5 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None
            r5.<init>(r6, r7)
            return r5
        L_0x0032:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r5 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            r5.<init>(r9, r10)
            r5.mIsChanged = r1
            return r5
        L_0x003a:
            boolean r0 = r11.equals(r10)
            if (r0 != 0) goto L_0x004e
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            boolean r2 = r9.equals(r0)
            if (r2 == 0) goto L_0x004e
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r5 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            r5.<init>(r0, r10)
            return r5
        L_0x004e:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None
            r0.<init>(r2, r3)
            int[] r3 = com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag
            int r4 = r11.ordinal()
            r3 = r3[r4]
            r4 = 1
            if (r3 == r4) goto L_0x0094
            r4 = 2
            if (r3 == r4) goto L_0x0090
            r4 = 3
            if (r3 == r4) goto L_0x0072
            r2 = 5
            if (r3 == r2) goto L_0x0094
            r1 = 6
            if (r3 == r1) goto L_0x0090
            r1 = 7
            if (r3 == r1) goto L_0x0090
            goto L_0x00d7
        L_0x0072:
            boolean r3 = r5.isActionStatusFlag(r10)
            if (r3 == 0) goto L_0x0085
            boolean r5 = r5.isDirectionFlag(r9)
            if (r5 == 0) goto L_0x00d7
            r0.mDirection = r2
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted
            r0.mAction = r5
            goto L_0x00d7
        L_0x0085:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted
            boolean r5 = r10.equals(r5)
            if (r5 == 0) goto L_0x00d7
            r0.mIsChanged = r1
            goto L_0x00d7
        L_0x0090:
            r5.handleNewMsgResponse(r9, r10, r0)
            goto L_0x00d7
        L_0x0094:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete
            boolean r2 = r10.equals(r5)
            if (r2 == 0) goto L_0x00cd
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x00ab
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud
            r0.mDirection = r1
            r0.mAction = r5
            goto L_0x00d7
        L_0x00ab:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x00ba
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            r0.mDirection = r1
            r0.mAction = r5
            goto L_0x00d7
        L_0x00ba:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud
            boolean r5 = r9.equals(r5)
            if (r5 != 0) goto L_0x00ca
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            boolean r5 = r9.equals(r5)
            if (r5 == 0) goto L_0x00d7
        L_0x00ca:
            r0.mIsChanged = r1
            goto L_0x00d7
        L_0x00cd:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted
            boolean r5 = r10.equals(r5)
            if (r5 == 0) goto L_0x00d7
            r0.mIsChanged = r1
        L_0x00d7:
            java.lang.String r5 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "dbIndex: "
            r1.append(r2)
            r1.append(r6)
            java.lang.String r6 = ", bufferId: "
            r1.append(r6)
            r1.append(r7)
            java.lang.String r6 = ", getSetFlagsForMsgResponse, origDir: "
            r1.append(r6)
            r1.append(r9)
            java.lang.String r6 = " origAction: "
            r1.append(r6)
            r1.append(r10)
            java.lang.String r6 = " msgResponse: "
            r1.append(r6)
            r1.append(r11)
            java.lang.String r6 = ", sync flag result :"
            r1.append(r6)
            java.lang.String r6 = r0.toString()
            r1.append(r6)
            java.lang.String r6 = r1.toString()
            android.util.Log.d(r5, r6)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule.getSetFlagsForMsgResponse(int, long, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag):com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet");
    }

    private void handleNewMsgResponse(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, ParamSyncFlagsSet paramSyncFlagsSet) {
        if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            updateRuleWithAction(directionFlag, paramSyncFlagsSet, actionStatusFlag);
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload)) {
            paramSyncFlagsSet.mIsChanged = false;
        } else {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad;
            if (actionStatusFlag.equals(actionStatusFlag2)) {
                paramSyncFlagsSet.mIsChanged = false;
                paramSyncFlagsSet.mAction = actionStatusFlag2;
                paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Downloading;
            }
        }
    }

    private void updateRuleWithAction(CloudMessageBufferDBConstants.DirectionFlag directionFlag, ParamSyncFlagsSet paramSyncFlagsSet, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag) {
        if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
            paramSyncFlagsSet.mAction = actionStatusFlag;
        } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
            paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice;
            paramSyncFlagsSet.mAction = actionStatusFlag;
        } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
            paramSyncFlagsSet.mIsChanged = false;
        }
    }

    private boolean isActionStatusFlag(CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag) {
        return actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.None) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri);
    }

    private boolean isDirectionFlag(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }

    private boolean isChangedDirection(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent);
    }

    private boolean isChangedDeleteDirection(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent);
    }

    private boolean isChangedUpdateDirection(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent);
    }

    public ParamSyncFlagsSet getSetFlagsForCldResponse(int i, long j, CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, CloudMessageBufferDBConstants.CloudResponseFlag cloudResponseFlag) {
        CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.Done;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.None;
        ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(directionFlag2, actionStatusFlag2);
        int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag[cloudResponseFlag.ordinal()];
        if (i2 != 1) {
            if (i2 == 2) {
                CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                if (actionStatusFlag.equals(actionStatusFlag3)) {
                    if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                        paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                        paramSyncFlagsSet.mAction = actionStatusFlag3;
                    } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                        paramSyncFlagsSet.mIsChanged = false;
                    }
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                    paramSyncFlagsSet.mIsChanged = false;
                }
            } else if (i2 == 3) {
                if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                    if (handleActionStatusFlagUpdateWhenSetDelete(directionFlag)) {
                        paramSyncFlagsSet.mDirection = directionFlag2;
                        paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                    }
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                    if (handleActionStatusFlagDeleteWhenSetDelete(directionFlag)) {
                        paramSyncFlagsSet.mDirection = directionFlag2;
                        paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                    }
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Insert) || actionStatusFlag.equals(actionStatusFlag2)) {
                    if (handleActionStatusFlagInsertOrNoneWhenSetDelete(directionFlag)) {
                        paramSyncFlagsSet.mDirection = directionFlag2;
                        paramSyncFlagsSet.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                    }
                } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
                    paramSyncFlagsSet.mIsChanged = false;
                }
            }
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud)) {
                paramSyncFlagsSet.mDirection = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                paramSyncFlagsSet.mAction = actionStatusFlag;
            } else if (directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) {
                paramSyncFlagsSet.mIsChanged = false;
            }
        } else if (actionStatusFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted)) {
            paramSyncFlagsSet.mIsChanged = false;
        }
        String str = TAG;
        Log.d(str, "dbIndex: " + i + ", bufferId: " + j + ", getSetFlagsForCldResponse, origDir: " + directionFlag + " origAction: " + actionStatusFlag + " cldResponse: " + cloudResponseFlag + ", sync flag result :" + paramSyncFlagsSet.toString());
        return paramSyncFlagsSet;
    }

    /* renamed from: com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        /* JADX WARNING: Can't wrap try/catch for region: R(44:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|(2:33|34)|35|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|58) */
        /* JADX WARNING: Can't wrap try/catch for region: R(45:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|(2:21|22)|23|25|26|27|(2:29|30)|31|(2:33|34)|35|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|58) */
        /* JADX WARNING: Can't wrap try/catch for region: R(46:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|16|17|18|19|(2:21|22)|23|25|26|27|(2:29|30)|31|(2:33|34)|35|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|58) */
        /* JADX WARNING: Can't wrap try/catch for region: R(49:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|21|22|23|25|26|27|(2:29|30)|31|33|34|35|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|58) */
        /* JADX WARNING: Can't wrap try/catch for region: R(50:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|21|22|23|25|26|27|29|30|31|33|34|35|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|58) */
        /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0039 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0043 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x008a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x0094 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x009e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00b2 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x00bc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x00c6 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x00d2 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x00de */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$CloudResponseFlag[] r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.CloudResponseFlag.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag = r0
                r1 = 1
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$CloudResponseFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.CloudResponseFlag.Inserted     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$CloudResponseFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.CloudResponseFlag.SetRead     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$CloudResponseFlag     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$CloudResponseFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.CloudResponseFlag.SetDelete     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag[] r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag = r3
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ NoSuchFieldError -> 0x0039 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0039 }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x0039 }
            L_0x0039:
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x0043 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r3[r4] = r0     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x004d }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ NoSuchFieldError -> 0x004d }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x004d }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x004d }
            L_0x004d:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x0058 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Cancel     // Catch:{ NoSuchFieldError -> 0x0058 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0058 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0058 }
            L_0x0058:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x0063 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload     // Catch:{ NoSuchFieldError -> 0x0063 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0063 }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x0063 }
            L_0x0063:
                r5 = 6
                int[] r6 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x006e }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri     // Catch:{ NoSuchFieldError -> 0x006e }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x006e }
                r6[r7] = r5     // Catch:{ NoSuchFieldError -> 0x006e }
            L_0x006e:
                r6 = 7
                int[] r7 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x0079 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri     // Catch:{ NoSuchFieldError -> 0x0079 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0079 }
                r7[r8] = r6     // Catch:{ NoSuchFieldError -> 0x0079 }
            L_0x0079:
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag[] r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.values()
                int r7 = r7.length
                int[] r7 = new int[r7]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = r7
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Receiving     // Catch:{ NoSuchFieldError -> 0x008a }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x008a }
                r7[r8] = r1     // Catch:{ NoSuchFieldError -> 0x008a }
            L_0x008a:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0094 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sent     // Catch:{ NoSuchFieldError -> 0x0094 }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x0094 }
                r1[r7] = r0     // Catch:{ NoSuchFieldError -> 0x0094 }
            L_0x0094:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x009e }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Received     // Catch:{ NoSuchFieldError -> 0x009e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009e }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009e }
            L_0x009e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sending     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00b2 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.SendFail     // Catch:{ NoSuchFieldError -> 0x00b2 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b2 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x00b2 }
            L_0x00b2:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00bc }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Read     // Catch:{ NoSuchFieldError -> 0x00bc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00bc }
                r0[r1] = r5     // Catch:{ NoSuchFieldError -> 0x00bc }
            L_0x00bc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00c6 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Delete     // Catch:{ NoSuchFieldError -> 0x00c6 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c6 }
                r0[r1] = r6     // Catch:{ NoSuchFieldError -> 0x00c6 }
            L_0x00c6:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00d2 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Cancel     // Catch:{ NoSuchFieldError -> 0x00d2 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d2 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d2 }
            L_0x00d2:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00de }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Starred     // Catch:{ NoSuchFieldError -> 0x00de }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00de }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00de }
            L_0x00de:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00ea }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.UnStarred     // Catch:{ NoSuchFieldError -> 0x00ea }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ea }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00ea }
            L_0x00ea:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule.AnonymousClass1.<clinit>():void");
        }
    }

    private boolean handleActionStatusFlagDeleteWhenSetDelete(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice);
    }

    private boolean handleActionStatusFlagInsertOrNoneWhenSetDelete(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.NmsEvent) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }

    private boolean handleActionStatusFlagUpdateWhenSetDelete(CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        return directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || directionFlag.equals(CloudMessageBufferDBConstants.DirectionFlag.Done);
    }
}
