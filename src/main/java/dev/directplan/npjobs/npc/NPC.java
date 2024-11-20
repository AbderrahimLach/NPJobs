package dev.directplan.npjobs.npc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.directplan.npjobs.Tickable;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author DirectPlan
 */
public interface NPC extends Observable, Tickable {

    /**
     * Gets the uuid of this npc.
     *
     * @return uuid of this npc.
     */
    UUID getId();

    /**
     * Gets the name of this npc.
     *
     * @return name of this npc.
     */
    String getName();

    /**
     * Gets the location of this npc.
     *
     * @return location of this npc.
     */
    Location getLocation();

    /**
     * Gets the skin of this npc.
     *
     * @return skin of this npc.
     */
    @Nullable Skin getSkin();

    /**
     * Gets whether this npc has spawned.
     *
     * @return whether this npc has spawned.
     */
    boolean isSpawned();

    /**
     * Gets whether this npc is currently navigating a certain path.
     *
     * @return whether this npc is currently navigating a certain path.
     */
    boolean isNavigating();

    /**
     * Gets whether this npc is currently jumping.
     *
     * @return whether this npc is currently jumping.
     */
    boolean isJumping();

    /**
     * Sets the skin of this npc to the provided {@code skin}.
     *
     * @param skin skin to set.
     */
    void setSkin(@NotNull Skin skin);

    /**
     * Sets the held item of this npc to the specified {@code item}.
     *
     * @param item item to set.
     */
    void setHeldItem(@NotNull ItemStack item);

    /**
     * Sets whether this npc should be {@code sneaking}.
     *
     * @param sneaking whether to sneak.
     */
    void setSneaking(boolean sneaking);

    /**
     * Makes this npc play a swing animation. Preferably using their main hand.
     */
    void playSwingAnimation();

    /**
     * Makes this npc jump.
     * <p>
     * The jump function is interruptible, which causes the worker to "fly"
     * if triggered repeatedly.
     */
    void jump();

    /**
     * Teleports this npc to the specified {@code location}.
     *
     * @param location where to teleport.
     */
    void teleport(@NotNull Location location);

    /**
     * Spawns this npc on the specified {@code location}.
     *
     * @param location where to spawn.
     */
    void spawn(@NotNull Location location);

    /**
     * Walks the worker to the specified {@code location}
     * and whether the worker should be {@code sprinting}.
     *
     * @param location walk destination
     * @param sprinting whether to sprint
     */
    void walkTo(@NotNull Location location, boolean sprinting);

    /**
     * Removes/de-spawns this npc.
     */
    void remove();

    record Skin(String texture, String signature) {

        private static final Cache<String, CompletableFuture<Skin>> SKIN_CACHE = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();

        private static final Gson GSON = new GsonBuilder().create();

        private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

        private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
        private static final String SESSION_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

        public static CompletableFuture<Skin> getSkin(final String username) {
            String usernameKey = username.toLowerCase();

            CompletableFuture<Skin> skinFuture = SKIN_CACHE.getIfPresent(usernameKey);
            if (skinFuture != null) return skinFuture;

            skinFuture = CompletableFuture.supplyAsync(() -> {

                JsonObject profileInfo = getJson(String.format(PROFILE_URL, username));
                String id = profileInfo.get("id").getAsString();

                JsonObject textures = getJson(String.format(SESSION_PROFILE_URL, id))
                        .getAsJsonArray("properties")
                        .get(0)
                        .getAsJsonObject();

                String texture = textures.get("value").getAsString();
                String signature = textures.get("signature").getAsString();

                return new Skin(texture, signature);
            });

            SKIN_CACHE.put(usernameKey, skinFuture);
            return skinFuture;
        }

        private static JsonObject getJson(String url) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(new URI(url))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                return GSON.fromJson(response.body(), JsonObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void apply(GameProfile gameProfile) {
            PropertyMap propertyMap = gameProfile.getProperties();
            // Property instances are not hashed solely by their name, why?
            propertyMap.removeAll("textures");
            propertyMap.put("textures", toMojangProperty());
        }

        public Property toMojangProperty() {
            return new Property("textures", texture, signature);
        }
    }
}
