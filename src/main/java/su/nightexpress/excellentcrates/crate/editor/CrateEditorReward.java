package su.nightexpress.excellentcrates.crate.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentcrates.ExcellentCrates;
import su.nightexpress.excellentcrates.api.crate.ICrate;
import su.nightexpress.excellentcrates.api.crate.ICrateReward;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.editor.CrateEditorHandler;
import su.nightexpress.excellentcrates.editor.CrateEditorType;

import java.util.Arrays;

public class CrateEditorReward extends AbstractMenu<ExcellentCrates> {

    private final ICrateReward reward;

    public CrateEditorReward(@NotNull ExcellentCrates plugin, @NotNull ICrateReward reward) {
        super(plugin, CrateEditorHandler.CRATE_REWARD_MAIN, "");
        this.reward = reward;
        ICrate crate = reward.getCrate();

        EditorInput<ICrateReward, CrateEditorType> input = (player, reward2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case CRATE_REWARD_CHANGE_CHANCE -> {
                    double chance = StringUtil.getDouble(StringUtil.colorOff(msg), -1);
                    if (chance < 0) {
                        EditorManager.error(player, EditorManager.ERROR_NUM_INVALID);
                        return false;
                    }
                    reward.setChance(chance);
                }
                case CRATE_REWARD_CHANGE_COMMANDS -> reward.getCommands().add(StringUtil.colorOff(msg));
                case CRATE_REWARD_CHANGE_NAME -> reward.setName(msg);
                case CRATE_REWARD_CHANGE_WIN_LIMITS_AMOUNT -> reward.setWinLimitAmount(StringUtil.getInteger(StringUtil.colorOff(msg), -1, true));
                case CRATE_REWARD_CHANGE_WIN_LIMITS_COOLDOWN -> reward.setWinLimitCooldown(StringUtil.getInteger(StringUtil.colorOff(msg), 0, true));
                default -> { }
            }

            reward.getCrate().save();
            return true;
        };

        IMenuClick click = (player, type, e) -> {
            ClickType clickType = e.getClick();
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    if (crate.getEditor() instanceof CrateEditorCrate editorCrate) {
                        editorCrate.getEditorRewards().open(player, 1);
                    }
                }
                return;
            }

            if (type instanceof CrateEditorType type2) {
                switch (type2) {
                    case CRATE_REWARD_DELETE -> {
                        if (!e.isShiftClick()) return;

                        reward.clear();
                        crate.removeReward(reward);
                        crate.save();
                        if (crate.getEditor() instanceof CrateEditorCrate editorCrate) {
                            editorCrate.getEditorRewards().open(player, 1);
                        }
                        return;
                    }
                    case CRATE_REWARD_CHANGE_NAME -> {
                        if (e.isRightClick()) {
                            reward.setName(ItemUtil.getItemName(reward.getPreview()));
                            break;
                        }
                        EditorManager.startEdit(player, reward, type2, input);
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_REWARD_ENTER_DISPLAY_NAME).getLocalized());
                        player.closeInventory();
                        return;
                    }
                    case CRATE_REWARD_CHANGE_PREVIEW -> {
                        if (e.isRightClick()) {
                            PlayerUtil.addItem(player, reward.getPreview());
                            return;
                        }
                        ItemStack cursor = e.getCursor();
                        if (cursor != null && !cursor.getType().isAir()) {
                            reward.setPreview(cursor);
                            e.getView().setCursor(null);
                        }
                    }
                    case CRATE_REWARD_CHANGE_BROADCAST -> reward.setBroadcast(!reward.isBroadcast());
                    case CRATE_REWARD_CHANGE_ITEMS -> {
                        new ContentEditor(reward).open(player, 1);
                        return;
                    }
                    case CRATE_REWARD_CHANGE_CHANCE -> {
                        EditorManager.startEdit(player, reward, type2, input);
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_REWARD_ENTER_CHANCE).getLocalized());
                        player.closeInventory();
                        return;
                    }
                    case CRATE_REWARD_CHANGE_COMMANDS -> {
                        if (e.isRightClick()) {
                            reward.getCommands().clear();
                        }
                        else {
                            EditorManager.startEdit(player, reward, type2, input);
                            EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_REWARD_ENTER_COMMAND).getLocalized());
                            EditorManager.sendCommandTips(player);
                            player.closeInventory();
                            return;
                        }
                    }
                    case CRATE_REWARD_CHANGE_WIN_LIMITS -> {
                        if (e.isLeftClick()) {
                            EditorManager.startEdit(player, reward, CrateEditorType.CRATE_REWARD_CHANGE_WIN_LIMITS_AMOUNT, input);
                            EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_REWARD_ENTER_WIN_LIMIT_AMOUNT).getLocalized());
                        }
                        else {
                            EditorManager.startEdit(player, reward, CrateEditorType.CRATE_REWARD_CHANGE_WIN_LIMITS_COOLDOWN, input);
                            EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_REWARD_ENTER_WIN_LIMIT_COOLDOWN).getLocalized());
                        }
                        player.closeInventory();
                        return;
                    }
                    default -> { }
                }
                crate.save();
                this.open(player, 1);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, CrateEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        if (menuItem.getType() == CrateEditorType.CRATE_REWARD_CHANGE_PREVIEW) {
            item.setType(this.reward.getPreview().getType());
            item.setAmount(this.reward.getPreview().getAmount());
        }

        ItemUtil.replace(item, this.reward.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }

    static class ContentEditor extends AbstractMenu<ExcellentCrates> {

        private final ICrateReward reward;

        public ContentEditor(@NotNull ICrateReward reward) {
            super(reward.getCrate().plugin(), "Reward Content", 27);
            this.reward = reward;
        }

        @Override
        public boolean destroyWhenNoViewers() {
            return true;
        }

        @Override
        public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
            return false;
        }

        @Override
        public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
            inventory.setContents(this.reward.getItems().stream().map(ItemStack::new).toList().toArray(new ItemStack[0]));
        }

        @Override
        public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

        }

        @Override
        public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
            Inventory inventory = e.getInventory();
            ItemStack[] items = new ItemStack[this.getSize()];

            for (int slot = 0; slot < items.length; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null) continue;

                items[slot] = new ItemStack(item);
            }

            this.reward.setItems(Arrays.asList(items));
            this.reward.getCrate().save();
            super.onClose(player, e);

            plugin.runTask(c -> this.reward.getEditor().open(player, 1), false);
        }
    }
}
