package com.tonic.api.widgets;

import com.tonic.Logger;
import com.tonic.Static;
import com.tonic.data.TileItemEx;
import com.tonic.data.TileObjectEx;
import com.tonic.queries.InventoryQuery;
import com.tonic.data.ItemContainerEx;
import com.tonic.data.ItemEx;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;

import java.util.List;
import java.util.function.Predicate;

/**
 * Inventory automation api
 */
public class InventoryAPI
{
    /**
     * get all items in your inventory
     * @return List<ItemEx>
     */
    public static List<ItemEx> getItems()
    {
        return Static.invoke(() -> InventoryQuery.fromInventoryId(InventoryID.INV).collect());
    }

    /**
     * get an item in your inventory by id
     * @param itemId item id
     * @return ItemEx
     */
    public static ItemEx getItem(int itemId)
    {
        return Static.invoke(() -> InventoryQuery.fromInventoryId(InventoryID.INV).withId(itemId).first());
    }

    /**
     * get an item in your inventory by name
     * @param itemName item name
     * @return ItemEx
     */
    public static ItemEx getItem(String itemName)
    {
        return Static.invoke(() -> InventoryQuery.fromInventoryId(InventoryID.INV).withName(itemName).first());
    }

    /**
     * get an item in your inventory by predicate
     * @param predicate predicate
     * @return ItemEx
     */
    public static ItemEx getItem(Predicate<ItemEx> predicate)
    {
        return Static.invoke(() -> InventoryQuery.fromInventoryId(InventoryID.INV).keepIf(predicate).first());
    }

    /**
     * Wields/wears/equips an item from inventory.
     * Bank-safe: Uses correct widget when bank is open.
     * @param item item to wield
     */
    public static void wield(ItemEx item)
    {
        if(item == null)
            return;

        // Use bankside widget when bank is open (prevents bank from closing)
        if (BankAPI.isOpen()) {
            // Get the bankside inventory widget
            Widget banksideInv = WidgetAPI.get(InterfaceID.Bankside.ITEMS);
            if (banksideInv != null && banksideInv.getChildren() != null && item.getSlot() < banksideInv.getChildren().length) {
                Widget itemWidget = banksideInv.getChild(item.getSlot());
                if (itemWidget != null) {
                    // Dynamically find and use the Wield/Wear/Equip action
                    WidgetAPI.interact(itemWidget, "Wield", "Wear", "Equip");
                    return;
                }
            }
            // Fallback to old method if widget lookup fails
            Logger.warn("Failed to get bankside widget for item, falling back to action index 3");
            WidgetAPI.interact(3, InterfaceID.Bankside.ITEMS, item.getSlot(), item.getId());
        } else {
            // Bank closed: Use regular inventory widget with action index 3 (works fine when bank is closed)
            WidgetAPI.interact(3, InterfaceID.Inventory.ITEMS, item.getSlot(), item.getId());
        }
    }

    /**
     * Wields/wears/equips an item from inventory by id.
     * Bank-safe: Uses correct widget when bank is open.
     * @param itemId item id to wield
     */
    public static void wield(int itemId)
    {
        ItemEx item = getItem(itemId);
        if(item == null) {
            Logger.warn("Item not found in inventory: " + itemId);
            return;
        }
        wield(item);
    }

    /**
     * Wields/wears/equips an item from inventory by name.
     * Bank-safe: Uses correct widget when bank is open.
     * @param itemName item name to wield
     */
    public static void wield(String itemName)
    {
        ItemEx item = getItem(itemName);
        if(item == null) {
            Logger.warn("Item not found in inventory: " + itemName);
            return;
        }
        wield(item);
    }

    /**
     * interact with an item in your inventory by action name
     * @param item item
     * @param action action name
     */
    public static void interact(ItemEx item, String action)
    {
        // Bank-safe wield: Use dedicated method
        if (action.equalsIgnoreCase("wield") || action.equalsIgnoreCase("wear") || action.equalsIgnoreCase("equip")) {
            wield(item);
            return;
        }
        itemAction(item.getSlot(), item.getId(), getAction(item, action));
    }

    /**
     * interact with an item in your inventory by id and action name
     * @param itemId item id
     * @param action action name
     */
    public static void interact(int itemId, String action)
    {
        ItemEx item = getItem(itemId);
        if(item == null)
        {
            Logger.warn("Item not found in inventory: " + itemId);
            return;
        }

        // Bank-safe wield: Use dedicated method
        if (action.equalsIgnoreCase("wield") || action.equalsIgnoreCase("wear") || action.equalsIgnoreCase("equip")) {
            wield(item);
            return;
        }
        itemAction(item.getSlot(), item.getId(), getAction(item, action));
    }

    /**
     * interact with an item in your inventory by action index
     * @param item item
     * @param action action index
     */
    public static void interact(ItemEx item, int action)
    {
        if(item == null)
            return;
        itemAction(item.getSlot(), item.getId(), action);
    }

    /**
     * interact with an item in your inventory by id and action index
     * @param itemId item id
     * @param action action index
     */
    public static void interact(int itemId, int action) {
        ItemEx item = getItem(itemId);
        if(item != null) {
            itemAction(item.getSlot(), item.getId(), action);
        }
    }

    /**
     * interact with the first item found in your inventory by ids and action index
     * @param itemIds item ids
     * @param action action index
     */
    public static void interact(int[] itemIds, int action) {
        for(int itemId : itemIds)
        {
            ItemEx item = getItem(itemId);
            if(item != null) {
                itemAction(item.getSlot(), item.getId(), action);
                return;
            }
        }
    }

    /**
     * interact with an item in your inventory by name and action index
     * @param itemName item name
     * @param action action index
     */
    public static void interact(String itemName, int action) {
        ItemEx item = getItem(itemName);
        if(item != null) {
            itemAction(item.getSlot(), item.getId(), action);
        }
    }

    /**
     * interact with an item in your inventory by slot, id and action index
     * @param slot slot
     * @param id id
     * @param action action index
     */
    public static void itemAction(int slot, int id, int action) {
        if(id == 6512 || id == -1)
            return;

        WidgetAPI.interact(action, InterfaceID.Inventory.ITEMS, slot, id);
    }

    /**
     * get the action index for an item action name
     * @param item item
     * @param option action name
     * @return action index
     */
    private static int getAction(ItemEx item, String option)
    {
        option = option.toLowerCase();
        switch (option)
        {
            case "drop":
                return 7;
            case "examine":
                return 10;
            case "wear":
            case "wield":
            case "equip":
                return 3;
            case "rub":
                return 6;
        }
        String[] actions = item.getActions();
        int index = -1;
        for(int i = 0; i < actions.length; i++)
        {
            if(actions[i] != null && actions[i].toLowerCase().contains(option))
            {
                index = i;
                break;
            }
        }
        return (index < 4) ? index + 2 : index + 3;
    }

    /**
     * drop all items from your inventory by list of ids
     * @param ids item ids to drop
     * @return number of ticks it will take
     */
    public static int dropAll(List<Integer> ids)
    {
        int count = 0;
        for(int id : ids)
        {
            count += dropAll(id);
        }
        return (int) Math.ceil((double) count / 10);
    }

    /**
     * drop all items from your inventory by list of ids
     * @param ids item ids to drop
     * @return number of ticks it will take
     */
    public static int dropAll(int... ids)
    {
        int count = 0;
        for(int id : ids)
        {
            count = dropAll(id);
        }
        return (int) Math.ceil((double) count / 10);
    }

    /**
     * drop all items from your inventory by list of ids
     * @param id item id to drop
     * @return number of ticks it will take
     */
    public static int dropAll(int id)
    {
        ItemContainerEx inventory = new ItemContainerEx(InventoryID.INV);
        int count = 0;
        for(ItemEx item : inventory.getItems())
        {
            if(item.getId() == id)
            {
                count++;
                InventoryAPI.interact(item, 7);
            }
        }
        return (int) Math.ceil((double) count / 10);
    }

    /**
     * check if your inventory is full
     * @return bool
     */
    public static boolean isFull()
    {
        return getEmptySlots() <= 0;
    }

    /**
     * check if your inventory is empty
     * @return bool
     */
    public static boolean isEmpty()
    {
        return getEmptySlots() == 28;
    }

    /**
     * get the number of empty slots in your inventory
     * @return int
     */
    public static int getEmptySlots() {
        ItemContainerEx inventory = new ItemContainerEx(InventoryID.INV);
        return 28 - inventory.getItems().size();
    }

    /**
     * check if your inventory contains all the specified item ids
     * @param itemIds item ids
     * @return bool
     */
    public static boolean contains(int... itemIds)
    {
        ItemContainerEx inventory = new ItemContainerEx(InventoryID.INV);
        for(int itemId : itemIds)
        {
            if(inventory.getFirst(itemId) == null)
                return false;
        }
        return true;
    }

    /**
     * check if your inventory contains any of the specified item ids
     * @param itemIds item ids
     * @return bool
     */
    public static boolean containsAny(int... itemIds)
    {
        ItemContainerEx inventory = new ItemContainerEx(InventoryID.INV);
        for(int itemId : itemIds)
        {
            if(inventory.getFirst(itemId) != null)
                return true;
        }
        return false;
    }

    /**
     * check if your inventory contains all the specified item names
     * @param itemNames item names
     * @return bool
     */
    public static boolean contains(String... itemNames)
    {
        ItemContainerEx inventory = new ItemContainerEx(InventoryID.INV);
        for(String name : itemNames)
        {
            if(inventory.getFirst(name) == null)
                return false;
        }
        return true;
    }

    /**
     * check if your inventory contains any of the specified item names
     * @param itemNames item names
     * @return bool
     */
    public static boolean containsAny(String... itemNames)
    {
        ItemContainerEx inventory = new ItemContainerEx(InventoryID.INV);
        for(String name : itemNames)
        {
            if(inventory.getFirst(name) != null)
                return true;
        }
        return false;
    }

    /**
     * count the total number of items in your inventory by ids
     * @param itemIds item ids
     * @return int
     */
    public static int count(int... itemIds)
    {
        return InventoryQuery.fromInventoryId(InventoryID.INV).withId(itemIds).count();
    }

    /**
     * count the total number of items in your inventory by ids
     * @param itemIds item ids
     * @return int
     */
    public static int canonicalCount(int... itemIds)
    {
        return InventoryQuery.fromInventoryId(InventoryID.INV).withCanonicalId(itemIds).count();
    }

    /**
     * count the total number of items in your inventory by names
     * @param itemNames item names
     * @return int
     */
    public static int count(String... itemNames)
    {
        return InventoryQuery.fromInventoryId(InventoryID.INV).withName(itemNames).count();
    }

    /**
     * use an item on a tile object
     * @param item item
     * @param tileObject tile object
     */
    public static void useOn(ItemEx item, TileObjectEx tileObject)
    {
        if(item == null || tileObject == null)
            return;

        WorldPoint wp = tileObject.getWorldLocation();
        WidgetAPI.onTileObject(InterfaceID.Inventory.ITEMS, item.getId(), item.getSlot(), tileObject.getId(), wp.getX(), wp.getY(), false);
    }

    /**
     * use an item on a ground item
     * @param item item
     * @param tileItem tile item
     */
    public static void useOn(ItemEx item, TileItemEx tileItem)
    {
        if(item == null || tileItem == null)
            return;

        WorldPoint wp = tileItem.getWorldLocation();
        WidgetAPI.onGroundItem(InterfaceID.Inventory.ITEMS, item.getId(), item.getSlot(), tileItem.getId(), wp.getX(), wp.getY(), false);
    }

    /**
     * use an item on a player
     * @param item item
     * @param player player
     */
    public static void useOn(ItemEx item, Player player)
    {
        if(item == null || player == null)
            return;

        WidgetAPI.onPlayer(InterfaceID.Inventory.ITEMS, item.getId(), item.getSlot(), player.getId(), false);
    }

    /**
     * use an item on an npc
     * @param item item
     * @param npc npc
     */
    public static void useOn(ItemEx item, NPC npc)
    {
        if(item == null || npc == null)
            return;

        WidgetAPI.onNpc(InterfaceID.Inventory.ITEMS, item.getId(), item.getSlot(), npc.getIndex(), false);
    }

    /**
     * use an item on another item in your inventory
     * @param item item
     * @param target target item
     */
    public static void useOn(ItemEx item, ItemEx target)
    {
        if(item == null || target == null)
            return;

        WidgetAPI.onWidget(InterfaceID.Inventory.ITEMS, item.getId(), item.getSlot(), InterfaceID.Inventory.ITEMS, target.getId(), target.getSlot());
    }

    /**
     * get the total count of an item in your inventory by id
     * @param id item id
     * @return int
     */
    public static int getCount(int id) {
        return getCount(id, true);
    }

    /**
     * get the total count of an item in your inventory by id
     * @param id item id
     * @param canonicalize whether to canonicalize the id (normalize all ids to un-noted version for the count)
     * @return int
     */
    public static int getCount(int id, boolean canonicalize) {
        if(canonicalize)
            return InventoryQuery.fromInventoryId(InventoryID.INV).withCanonicalId(id).count();
        return InventoryQuery.fromInventoryId(InventoryID.INV).withId(id).count();
    }
}
