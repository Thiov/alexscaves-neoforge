package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Method;

public class RenderSystemCompat {

    private static float shaderFogStart;
    private static float shaderFogEnd;

    private RenderSystemCompat() {
    }

    public static float getShaderFogStart() {
        return shaderFogStart;
    }

    public static void setShaderFogStart(float fogStart) {
        shaderFogStart = fogStart;
    }

    public static float getShaderFogEnd() {
        return shaderFogEnd;
    }

    public static void setShaderFogEnd(float fogEnd) {
        shaderFogEnd = fogEnd;
    }

    public static void setShaderTexture(Object texture) {
        try {
            invokeShaderTexture(texture);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to bind shader texture " + texture, e);
        }
    }

    public static boolean supportsShaderTexture() {
        for (Method method : RenderSystem.class.getMethods()) {
            if (method.getName().equals("setShaderTexture") && method.getParameterCount() == 2 && method.getParameterTypes()[0] == int.class) {
                return true;
            }
        }
        return false;
    }

    public static void enableDepthTest() {
        invokeOptional("enableDepthTest");
    }

    private static void invokeShaderTexture(Object texture) throws ReflectiveOperationException {
        Object textureView = texture instanceof Identifier resourceLocation ? resolveTextureView(resourceLocation) : texture;

        for (Method method : RenderSystem.class.getMethods()) {
            if (!method.getName().equals("setShaderTexture") || method.getParameterCount() != 2 || method.getParameterTypes()[0] != int.class) {
                continue;
            }
            Class<?> textureType = method.getParameterTypes()[1];
            if (texture != null && textureType.isInstance(texture)) {
                method.invoke(null, 0, texture);
                return;
            }
            if (textureView != null && textureType.isInstance(textureView)) {
                method.invoke(null, 0, textureView);
                return;
            }
        }
        throw new IllegalStateException("No compatible RenderSystem.setShaderTexture overload found for " + texture);
    }

    private static Object resolveTextureView(Identifier resourceLocation) throws ReflectiveOperationException {
        Object textureManager = Minecraft.getInstance().getTextureManager();
        Method getTexture = textureManager.getClass().getMethod("getTexture", Identifier.class);
        Object abstractTexture = getTexture.invoke(textureManager, resourceLocation);
        Method getTextureView = abstractTexture.getClass().getMethod("getTextureView");
        return getTextureView.invoke(abstractTexture);
    }

    private static void invokeOptional(String methodName) {
        try {
            Method method = RenderSystem.class.getMethod(methodName);
            method.invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
