package dev.reproachful.tappedlol.backpacks;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public final class Backpacks extends JavaPlugin {

    private Map<String, Backpack> backpacks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        backpacks = new HashMap<>();

        for (String id : getConfig().getConfigurationSection("backpacks").getKeys(false)) {
            Backpack backpack = new Backpack(id, getConfig().getString("backpacks." + id + ".name"), getConfig().getInt("backpacks." + id + ".size"), getConfig().getString("backpacks." + id + ".texture"));
            backpacks.put(id, backpack);
        }

        getServer().getPluginManager().registerEvents(new BackpackListener(this), this);
        getCommand("backpack").setExecutor(new BackpackCommand(this));
    }

    public ItemStack getBackpack(String identifier) {
        Backpack backpack = backpacks.getOrDefault(identifier, null);
        if (backpack == null) {
            return new ItemStack(Material.STONE);
        }
        String url = "http://textures.minecraft.net/texture/" + backpack.getTexture();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        headMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', backpack.getName()));

        NamespacedKey uuidKey = new NamespacedKey(this, "backpack-uuid");
        NamespacedKey sizeKey = new NamespacedKey(this, "backpack-size");
        PersistentDataContainer dataContainer = headMeta.getPersistentDataContainer();
        dataContainer.set(uuidKey, new UUIDDataType(), UUID.randomUUID());
        dataContainer.set(sizeKey, PersistentDataType.INTEGER, backpack.getSize());

        head.setItemMeta(headMeta);
        return head;
    }

    public ItemStack[] getBackpackContents(ItemStack backpack) throws IOException {
        NamespacedKey uuidKey = new NamespacedKey(this, "backpack-uuid");
        NamespacedKey contentKey = new NamespacedKey(this, "backpack-contents");
        ItemMeta meta = backpack.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        if (dataContainer.has(uuidKey, new UUIDDataType())) {
            if (dataContainer.has(contentKey, PersistentDataType.STRING)) {
                return itemStackArrayFromBase64(dataContainer.get(contentKey, PersistentDataType.STRING));
            }
        }
        return new ItemStack[0];
    }

    public ItemStack updateBackpack(ItemStack backpack, ItemStack[] contents) {
        NamespacedKey uuidKey = new NamespacedKey(this, "backpack-uuid");
        NamespacedKey contentKey = new NamespacedKey(this, "backpack-contents");
        ItemMeta meta = backpack.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        if (dataContainer.has(uuidKey, new UUIDDataType())) {
            dataContainer.set(contentKey, PersistentDataType.STRING, itemStackArrayToBase64(contents));
        }
        backpack.setItemMeta(meta);
        return backpack;
    }

    public boolean isBackpack(ItemStack item) {
        NamespacedKey uuidKey = new NamespacedKey(this, "backpack-uuid");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        return dataContainer.has(uuidKey, new UUIDDataType());

    }

    public int backpackSize(ItemStack item) {
        NamespacedKey uuidKey = new NamespacedKey(this, "backpack-uuid");
        NamespacedKey sizeKey = new NamespacedKey(this, "backpack-size");
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        if (dataContainer.has(uuidKey, new UUIDDataType()) && dataContainer.has(sizeKey, PersistentDataType.INTEGER)) {
            return dataContainer.get(sizeKey, PersistentDataType.INTEGER);
        }
        return 0;
    }

    /**
     * Gets an array of ItemStacks from Base64 string.
     * <p>
     * <p/>
     *
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    /**
     * A method to serialize an {@link ItemStack} array to Base64 String.
     * <p>
     * <p/>
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException
     */
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save itemstacks.", e);
        }
    }

    public Map<String, Backpack> getBackpacks() {
        return backpacks;
    }
}
