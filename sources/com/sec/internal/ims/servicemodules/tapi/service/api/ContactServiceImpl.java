package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.capability.Capabilities;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.contact.ContactUtil;
import com.gsma.services.rcs.contact.IContactService;
import com.gsma.services.rcs.contact.RcsContact;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactItem;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactPersisit;
import com.sec.internal.ims.servicemodules.tapi.service.utils.ContactInfo;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactServiceImpl extends IContactService.Stub {
    private static final String LOG_TAG = ContactServiceImpl.class.getSimpleName();
    private CapabilityDiscoveryService capabilityDiscoveryService;
    private Context mContext;
    private IPresenceModule presenceModule;

    private interface FilterContactInfo {
        boolean inScope(ContactInfo contactInfo);
    }

    public ContactServiceImpl(Context context) {
        this.mContext = null;
        this.presenceModule = null;
        this.capabilityDiscoveryService = null;
        this.presenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();
        this.capabilityDiscoveryService = (CapabilityDiscoveryService) ImsRegistry.getBinder("options", (String) null);
        this.mContext = context;
    }

    public RcsContact getRcsContact(ContactId contactId) throws ServerApiException {
        Capabilities capabilities = null;
        if (contactId == null || contactId.toString() == null) {
            return null;
        }
        ContactInfo contactInfo = getContactInfo(contactId);
        boolean z = true;
        if (contactInfo.getRegistrationState() != 1) {
            z = false;
        }
        boolean z2 = z;
        boolean isBlock = isBlock(contactInfo.getContact());
        if (z2) {
            capabilities = contactInfo.getCapabilities();
        }
        Capabilities capabilities2 = capabilities;
        String str = LOG_TAG;
        Log.d(str, "getRcsContact ContactId = " + contactId.toString() + ", contactInfo = " + contactInfo.toString() + ", registered = " + z2 + ", capApi = " + capabilities2 + ", DisplayName" + contactInfo.getDisplayName());
        return new RcsContact(contactInfo.getContact(), z2, capabilities2, contactInfo.getDisplayName(), getBlockTime(contactInfo.getContact()), isBlock);
    }

    private List<RcsContact> getRcsContacts(FilterContactInfo filterContactInfo) throws ServerApiException {
        RcsContact rcsContact;
        ArrayList arrayList = new ArrayList();
        Set<ContactId> contactIds = getContactIds();
        if (contactIds == null) {
            return null;
        }
        for (ContactId contactInfo : contactIds) {
            ContactInfo contactInfo2 = getContactInfo(contactInfo);
            if (!(contactInfo2 == null || !filterContactInfo.inScope(contactInfo2) || (rcsContact = getRcsContact(contactInfo2.getContact())) == null)) {
                arrayList.add(rcsContact);
            }
        }
        return arrayList;
    }

    public Set<ContactId> getContactIds() {
        HashSet hashSet = new HashSet();
        try {
            com.sec.ims.options.Capabilities[] allCapabilities = this.capabilityDiscoveryService.getAllCapabilities(0);
            if (allCapabilities == null) {
                Log.d(LOG_TAG, "capabilitiesArray = null");
                return null;
            }
            for (com.sec.ims.options.Capabilities uri : allCapabilities) {
                String extractNumberFromUri = PhoneUtils.extractNumberFromUri(uri.getUri().toString());
                if (extractNumberFromUri != null) {
                    hashSet.add(ContactUtil.getInstance(this.mContext).formatContact(extractNumberFromUri));
                }
            }
            return hashSet;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<RcsContact> getRcsContacts() throws ServerApiException {
        Log.d(LOG_TAG, "getRcsContacts");
        ArrayList arrayList = new ArrayList();
        ContactInfo contactInfo = new ContactInfo();
        try {
            com.sec.ims.options.Capabilities[] allCapabilities = this.capabilityDiscoveryService.getAllCapabilities(0);
            if (allCapabilities == null) {
                return null;
            }
            for (com.sec.ims.options.Capabilities capabilities : allCapabilities) {
                String extractNumberFromUri = PhoneUtils.extractNumberFromUri(capabilities.getUri().toString());
                if (extractNumberFromUri != null) {
                    ContactId contactId = new ContactId(extractNumberFromUri);
                    contactInfo.setRcsStatusTimestamp(capabilities.getTimestamp().getTime());
                    contactInfo.setRcsDisplayName(capabilities.getDisplayName());
                    int i = 2;
                    contactInfo.setRcsStatus(capabilities.isAvailable() ? 2 : 1);
                    if (capabilities.isAvailable()) {
                        i = 1;
                    }
                    contactInfo.setRegistrationState(i);
                    contactInfo.setContact(contactId);
                    contactInfo.setCapabilities(CapabilityServiceImpl.transferCapabilities(capabilities));
                    arrayList.add(new RcsContact(contactInfo.getContact(), contactInfo.getRegistrationState() == 1, contactInfo.getCapabilities(), contactInfo.getDisplayName(), getBlockTime(contactInfo.getContact()), isBlock(contactInfo.getContact())));
                }
            }
            return arrayList;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<RcsContact> getRcsContactsOnline() throws ServerApiException {
        Log.d(LOG_TAG, "getRcsContactsOnline");
        return getRcsContacts(new FilterContactInfo() {
            public boolean inScope(ContactInfo contactInfo) {
                return contactInfo.getRegistrationState() == 1;
            }
        });
    }

    public List<RcsContact> getRcsContactsSupporting(final String str) throws ServerApiException {
        Log.d(LOG_TAG, "getRcsContactsSupporting");
        return getRcsContacts(new FilterContactInfo() {
            public boolean inScope(ContactInfo contactInfo) {
                Set<String> supportedExtensions;
                Capabilities capabilities = contactInfo.getCapabilities();
                if (capabilities == null || (supportedExtensions = capabilities.getSupportedExtensions()) == null) {
                    return false;
                }
                for (String equals : supportedExtensions) {
                    if (equals.equals(str)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void blockContact(ContactId contactId) throws RemoteException {
        if (contactId != null) {
            String str = LOG_TAG;
            Log.d(str, "Block contact:" + contactId.toString());
            try {
                BlockContactPersisit.changeContactInfo(this.mContext, setBlockingState(contactId, ContactInfo.BlockingState.BLOCKED));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            throw new ServerApiException("contact is null!");
        }
    }

    public void unblockContact(ContactId contactId) throws RemoteException {
        if (contactId != null) {
            String str = LOG_TAG;
            Log.d(str, "unblockContact contact" + contactId.toString());
            try {
                BlockContactPersisit.changeContactInfo(this.mContext, setBlockingState(contactId, ContactInfo.BlockingState.NOT_BLOCKED));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            throw new ServerApiException("contact is null!");
        }
    }

    public ContactInfo setBlockingState(ContactId contactId, ContactInfo.BlockingState blockingState) throws RemoteException {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setBlockingState(blockingState);
        contactInfo.setBlockingTimestamp(System.currentTimeMillis());
        contactInfo.setContact(contactId);
        setContactInfo(contactInfo);
        return contactInfo;
    }

    public ContactInfo getContactInfo(ContactId contactId) throws ServerApiException {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setContact(contactId);
        try {
            setContactInfo(contactInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) throws RemoteException {
        PresenceInfo presenceInfo;
        contactInfo.setRcsStatus(8);
        contactInfo.setRegistrationState(0);
        contactInfo.setRcsStatusTimestamp(System.currentTimeMillis());
        String str = "tel:" + PhoneUtils.extractNumberFromUri(contactInfo.getContact().toString());
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        try {
            com.sec.ims.options.Capabilities capabilities = this.capabilityDiscoveryService.getCapabilities(ImsUri.parse(str), CapabilityRefreshType.ONLY_IF_NOT_FRESH.ordinal(), activeDataPhoneId);
            if (capabilities != null) {
                contactInfo.setRcsStatusTimestamp(capabilities.getTimestamp().getTime());
                contactInfo.setRcsDisplayName(capabilities.getDisplayName());
                int i = 2;
                contactInfo.setRcsStatus(capabilities.isAvailable() ? 2 : 1);
                if (capabilities.isAvailable()) {
                    i = 1;
                }
                contactInfo.setRegistrationState(i);
                String str2 = LOG_TAG;
                Log.d(str2, "RcsStatus:" + contactInfo.getRcsStatus() + "State:" + contactInfo.getRegistrationState());
                Capabilities transferCapabilities = CapabilityServiceImpl.transferCapabilities(capabilities);
                if (transferCapabilities != null) {
                    contactInfo.setCapabilities(transferCapabilities);
                }
                IPresenceModule iPresenceModule = this.presenceModule;
                if (iPresenceModule != null) {
                    presenceInfo = iPresenceModule.getPresenceInfoByContactId(str, activeDataPhoneId);
                    if (presenceInfo == null) {
                        presenceInfo = this.presenceModule.getPresenceInfo(ImsUri.parse(str), activeDataPhoneId);
                    }
                } else {
                    presenceInfo = null;
                }
                if (presenceInfo != null) {
                    Log.d(str2, "presenceInfo.getContactId() = " + presenceInfo.getContactId());
                    contactInfo.setPresenceInfo(presenceInfo);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Cursor getCursor(ContactId contactId) {
        return this.mContext.getContentResolver().query(Uri.parse("content://com.gsma.services.rcs.provider.blockedcontact/" + contactId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0021 A[Catch:{ all -> 0x0049, all -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0023 A[Catch:{ all -> 0x0049, all -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isBlock(com.gsma.services.rcs.contact.ContactId r5) {
        /*
            r4 = this;
            android.database.Cursor r4 = r4.getCursor(r5)
            if (r4 == 0) goto L_0x0017
            boolean r5 = r4.moveToFirst()     // Catch:{ all -> 0x0049 }
            if (r5 == 0) goto L_0x0017
            java.lang.String r5 = "key_blocked"
            int r5 = r4.getColumnIndex(r5)     // Catch:{ all -> 0x0049 }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ all -> 0x0049 }
            goto L_0x0019
        L_0x0017:
            java.lang.String r5 = ""
        L_0x0019:
            java.lang.String r0 = "BLOCKED"
            boolean r0 = r0.equals(r5)     // Catch:{ all -> 0x0049 }
            if (r0 == 0) goto L_0x0023
            r0 = 1
            goto L_0x0024
        L_0x0023:
            r0 = 0
        L_0x0024:
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x0049 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0049 }
            r2.<init>()     // Catch:{ all -> 0x0049 }
            java.lang.String r3 = "string blocked: "
            r2.append(r3)     // Catch:{ all -> 0x0049 }
            r2.append(r5)     // Catch:{ all -> 0x0049 }
            java.lang.String r5 = "count ==1 mIsBlocked: "
            r2.append(r5)     // Catch:{ all -> 0x0049 }
            r2.append(r0)     // Catch:{ all -> 0x0049 }
            java.lang.String r5 = r2.toString()     // Catch:{ all -> 0x0049 }
            android.util.Log.d(r1, r5)     // Catch:{ all -> 0x0049 }
            if (r4 == 0) goto L_0x0048
            r4.close()
        L_0x0048:
            return r0
        L_0x0049:
            r5 = move-exception
            if (r4 == 0) goto L_0x0054
            r4.close()     // Catch:{ all -> 0x0050 }
            goto L_0x0054
        L_0x0050:
            r4 = move-exception
            r5.addSuppressed(r4)
        L_0x0054:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.ContactServiceImpl.isBlock(com.gsma.services.rcs.contact.ContactId):boolean");
    }

    public long getBlockTime(ContactId contactId) {
        Cursor cursor = getCursor(contactId);
        try {
            long j = -1;
            if (true == isBlock(contactId) && cursor != null && cursor.moveToFirst()) {
                j = cursor.getLong(cursor.getColumnIndex(BlockContactItem.BlockDataItem.KEY_BLOCKING_TIMESTAMP));
            }
            if (cursor != null) {
                cursor.close();
            }
            return j;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }
}
