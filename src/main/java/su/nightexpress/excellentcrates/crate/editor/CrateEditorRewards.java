package su.nightexpress.excellentcrates.crate.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentcrates.ExcellentCrates;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.api.crate.ICrate;
import su.nightexpress.excellentcrates.api.crate.ICrateReward;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.CrateReward;
import su.nightexpress.excellentcrates.editor.CrateEditorHandler;
import su.nightexpress.excellentcrates.editor.CrateEditorType;

import java.util.ArrayList;
import java.util.List;

public class CrateEditorRewards extends AbstractMenuAuto<ExcellentCrates, ICrateReward> {

    private static int[]        objectSlots;
    private static String       objectName;
    private static List<String> objectLore;
    private final ICrate crate;

    public CrateEditorRewards(@NotNull ExcellentCrates plugin, @NotNull ICrate crate) {
        super(plugin, CrateEditorHandler.CRATE_REWARD_LIST, "");
        this.crate = crate;

        objectSlots = cfg.getIntArray("Object.Slots");
        objectName = StringUtil.color(cfg.getString("Object.Name", Placeholders.REWARD_NAME));
        objectLore = StringUtil.color(cfg.getStringList("Object.Lore"));

        EditorInput<ICrate, CrateEditorType> input = (player, crate2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());

            if (type == CrateEditorType.CRATE_REWARD_CREATE) {
                String id = EditorManager.fineId(msg);
                if (crate2.getReward(id) != null) {
                    EditorManager.error(player, plugin.getMessage(Lang.EDITOR_REWARD_ERROR_CREATE_EXIST).getLocalized());
                    return false;
                }
                ICrateReward reward = new CrateReward(crate2, id);
                crate2.addReward(reward);
            }

            crate2.save();
            return true;
        };

        IMenuClick click = (p, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    crate.getEditor().open(p, 1);
                }
                else this.onItemClickDefault(p, type2);
            }
            else if (type instanceof CrateEditorType type2) {
                if (type2 == CrateEditorType.CRATE_REWARD_CREATE) {
                    EditorManager.startEdit(p, crate, type2, input);
                    EditorManager.tip(p, plugin.getMessage(Lang.EDITOR_REWARD_ENTER_ID).getLocalized());
                    p.closeInventory();
                }
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
    public int[] getObjectSlots() {
        return objectSlots;
    }

    @Override
    @NotNull
    protected List<ICrateReward> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.crate.getRewards());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ICrateReward reward) {
        ItemStack item = new ItemStack(reward.getPreview());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(objectName);
        meta.setLore(objectLore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUtil.replace(item, reward.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull ICrateReward reward) {
        return (player1, type, e) -> {
            if (e.isShiftClick()) {
                // Reward position move.
                List<ICrateReward> all = new ArrayList<>(this.crate.getRewards());
                int index = all.indexOf(reward);
                int allSize = all.size();

                if (e.isLeftClick()) {
                    if (index + 1 >= allSize) return;

                    all.remove(index);
                    all.add(index + 1, reward);
                }
                else if (e.isRightClick()) {
                    if (index == 0) return;

                    all.remove(index);
                    all.add(index - 1, reward);
                }
                this.crate.setRewards(all);
                this.crate.save();
                this.open(player1, this.getPage(player1));
                return;
            }

            if (e.isLeftClick()) {
                reward.getEditor().open(player1, 1);
            }
        };
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
