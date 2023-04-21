package moe.minamigo.antiantidefraud;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedMain implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("com.miui.guardprovider")) {
            XposedBridge.log("[[[AntiAntiDefraud]]] Start to hook package " + lpparam.packageName);

            // Debug mode flag process
            final Class<?> guardApplication = XposedHelpers.findClass("com.miui.guardprovider.GuardApplication", lpparam.classLoader);
            if (guardApplication != null) {
                Field[] guardApplicationFields = guardApplication.getDeclaredFields();
                boolean debugMode = false;

                for (Field field : guardApplicationFields) {
                    if ("b".equals(field.getName())) {
                        try {
                            field.setAccessible(true);
                            field.set(guardApplication, true);
                            XposedBridge.log("[[[AntiAntiDefraud]]] Info: GuardProvider will work as debug mode!");
                            debugMode = true;
                        } catch (Throwable e) {
                            XposedBridge.log("[[[AntiAntiDefraud]]] Warn: Unable to enable debug mode!");
                            e.printStackTrace();
                        }
                    }
                }

                if (!debugMode) {
                    XposedBridge.log("[[[AntiAntiDefraud]]] Warning: GuardProvider debug mode flag not found!");
                }
            } else {
                XposedBridge.log("[[[AntiAntiDefraud]]] Warning: GuardApplication class not found. GuardProvider will not work as debug mode! ");
            }

            // Prevent miui from uploading app list
            final Class<?> antiDefraudAppManager = XposedHelpers.findClassIfExists("p.c", lpparam.classLoader);
            if (antiDefraudAppManager == null) {
                XposedBridge.log("[[[AntiAntiDefraud]]] Skip: AntiDefraudAppManager class not found.");
                return;
            } else {
                XposedBridge.log("[[[AntiAntiDefraud]]] Info: AntiDefraudAppManager class found.");
            }

            final Method[] methods = antiDefraudAppManager.getDeclaredMethods();
            Method getAllUnSystemAppsStatus = null;
            Method getAllDetectUnsafeApps = null;
            Method getUnSystemAppList = null;
            for (Method method : methods) {
                // getAllUnSystemAppsStatus
                if (method.getName().equals("b") && method.getParameterTypes().length == 1) {
                    getAllUnSystemAppsStatus = method;
                }

                // getAllDetectUnsafeApps
                if ("a".equals(method.getName()) && method.getParameterTypes().length == 0) {
                    getAllDetectUnsafeApps = method;
                }

                // getUnSystemAppList
                if ("g".equals(method.getName()) && method.getParameterTypes().length == 1) {
                    getUnSystemAppList = method;
                }
            }

            if (getAllUnSystemAppsStatus == null) {
                XposedBridge.log("[[[AntiAntiDefraud]]] Skip: getAllUnSystemAppsStatus method not found.");
                return;
            } else {
                XposedBridge.log("[[[AntiAntiDefraud]]] Info: getAllUnSystemAppsStatus method found.");
            }

            if (getAllDetectUnsafeApps == null) {
                XposedBridge.log("[[[AntiAntiDefraud]]] Skip: getAllDetectUnsafeApps method not found.");
                return;
            } else {
                XposedBridge.log("[[[AntiAntiDefraud]]] Info: getAllDetectUnsafeApps method found.");
            }

            if (getUnSystemAppList == null) {
                XposedBridge.log("[[[AntiAntiDefraud]]] Skip: getUnSystemAppList method not found.");
                return;
            } else {
                XposedBridge.log("[[[AntiAntiDefraud]]] Info: getUnSystemAppList method found.");
            }

            XposedBridge.hookMethod(getAllUnSystemAppsStatus, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                    XposedBridge.log("[[[AntiAntiDefraud]]] Rejected getAllUnSystemAppsStatus action.");
                    methodHookParam.setResult(null);
                }
            });

            XposedBridge.hookMethod(getAllDetectUnsafeApps, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                    XposedBridge.log("[[[AntiAntiDefraud]]] Rejected getAllDetectUnsafeApps action.");
                    methodHookParam.setResult(new String[0]);
                }
            });

            XposedBridge.hookMethod(getUnSystemAppList, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                    XposedBridge.log("[[[AntiAntiDefraud]]] Rejected getUnSystemAppList action.");
                    methodHookParam.setResult(new ArrayList<>());
                }
            });
        }
    }
}
