package com.sec.internal.helper.os;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.os.Parcel;
import com.sec.ims.extensions.ReflectionUtils;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.List;

public class LinkPropertiesWrapper {
    private final LinkProperties mLinkProperties;

    public LinkPropertiesWrapper() {
        try {
            this.mLinkProperties = LinkProperties.class.getConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }
    }

    public LinkPropertiesWrapper(LinkProperties linkProperties) {
        this.mLinkProperties = linkProperties;
    }

    public String getInterfaceName() {
        return this.mLinkProperties.getInterfaceName();
    }

    public List<LinkAddress> getLinkAddresses() {
        return this.mLinkProperties.getLinkAddresses();
    }

    public List<InetAddress> getDnsServers() {
        return this.mLinkProperties.getDnsServers();
    }

    public int describeContents() {
        return this.mLinkProperties.describeContents();
    }

    public ProxyInfo getHttpProxy() {
        return this.mLinkProperties.getHttpProxy();
    }

    public List<RouteInfo> getRoutes() {
        return this.mLinkProperties.getRoutes();
    }

    public void writeToParcel(Parcel parcel, int i) {
        this.mLinkProperties.writeToParcel(parcel, i);
    }

    public String getDomains() {
        return this.mLinkProperties.getDomains();
    }

    public String toString() {
        return this.mLinkProperties.toString();
    }

    public List<InetAddress> getAddresses() {
        try {
            return (List) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("getAddresses", new Class[0]), this.mLinkProperties, new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<InetAddress> getAllAddresses() {
        try {
            return (List) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("getAllAddresses", new Class[0]), this.mLinkProperties, new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isIdenticalInterfaceName(LinkPropertiesWrapper linkPropertiesWrapper) {
        try {
            return ((Boolean) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("isIdenticalInterfaceName", new Class[]{LinkProperties.class}), this.mLinkProperties, new Object[]{linkPropertiesWrapper.getLinkProperties()})).booleanValue();
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public LinkProperties getLinkProperties() {
        return this.mLinkProperties;
    }

    public boolean hasGlobalIPv6Address() {
        try {
            return ((Boolean) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("hasGlobalIPv6Address", new Class[0]), this.mLinkProperties, new Object[0])).booleanValue();
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasIPv6DefaultRoute() {
        try {
            return ((Boolean) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("hasIPv6DefaultRoute", new Class[0]), this.mLinkProperties, new Object[0])).booleanValue();
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasIPv4Address() {
        try {
            return ((Boolean) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("hasIPv4Address", new Class[0]), this.mLinkProperties, new Object[0])).booleanValue();
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InetAddress> getPcscfServers() {
        try {
            return (List) ReflectionUtils.invoke2(this.mLinkProperties.getClass().getMethod("getPcscfServers", new Class[0]), this.mLinkProperties, new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
