package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.recipes.CraftingHandler.SlotRange;
import fi.dy.masa.itemscroller.util.Constants;
import fi.dy.masa.itemscroller.util.InventoryUtils;
import net.minecraft.world.World;

public class RecipePattern
{
    private ItemStack result = InventoryUtils.EMPTY_STACK;
    private ItemStack[] recipe = new ItemStack[9];
    @Nullable private CraftingRecipe vanillaRecipe;

    public RecipePattern()
    {
        this.ensureRecipeSizeAndClearRecipe(9);
    }

    public void ensureRecipeSize(int size)
    {
        if (this.getRecipeLength() != size)
        {
            this.recipe = new ItemStack[size];
        }
    }

    public void clearRecipe()
    {
        Arrays.fill(this.recipe, InventoryUtils.EMPTY_STACK);
        this.result = InventoryUtils.EMPTY_STACK;
        this.vanillaRecipe = null;
    }

    public void ensureRecipeSizeAndClearRecipe(int size)
    {
        this.ensureRecipeSize(size);
        this.clearRecipe();
    }

    @Nullable
    public CraftingRecipe lookupVanillaRecipe(World world) {
        //Assume all recipes here are of type CraftingRecipe
        // 1.20.1にはCraftingRecipeInputが無いのでダミーのCraftingInventoryを組んで照合する
        this.vanillaRecipe = null;
        var mc = MinecraftClient.getInstance();
        int recipeSize;
        if (recipe.length == 4)
        {
            recipeSize = 2;
        }
        else if (recipe.length == 9)
        {
            recipeSize = 3;
        }
        else
        {
            return null;
        }

        CraftingInventory inv = new CraftingInventory(new ScreenHandler((ScreenHandlerType<?>) null, 0)
        {
            @Override
            public ItemStack quickMove(PlayerEntity player, int slot)
            {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean canUse(PlayerEntity player)
            {
                return true;
            }
        }, recipeSize, recipeSize);

        for (int i = 0; i < Math.min(recipeSize * recipeSize, this.recipe.length); ++i)
        {
            inv.setStack(i, this.recipe[i]);
        }

        for (CraftingRecipe match : mc.world.getRecipeManager().getAllMatches(RecipeType.CRAFTING, inv, world))
        {
            // 1.20.1ではgetOutput(registryManager)
            if (InventoryUtils.areStacksEqual(this.result, match.getOutput(world.getRegistryManager())))
            {
                this.vanillaRecipe = match;
                return match;
            }
        }
        return null;
    }

    public void storeCraftingRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui, boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        SlotRange range = CraftingHandler.getCraftingGridSlots(gui, slot);

        if (range != null)
        {
            if (slot.hasStack())
            {
                int gridSize = range.getSlotCount();
                int numSlots = gui.getScreenHandler().slots.size();

                this.ensureRecipeSizeAndClearRecipe(gridSize);

                for (int i = 0, s = range.getFirst(); i < gridSize && s < numSlots; i++, s++)
                {
                    Slot slotTmp = gui.getScreenHandler().getSlot(s);
                    this.recipe[i] = slotTmp.hasStack() ? slotTmp.getStack().copy() : InventoryUtils.EMPTY_STACK;
                }

                this.result = slot.getStack().copy();
                this.lookupVanillaRecipe(MinecraftClient.getInstance().world);
            }
            else if (clearIfEmpty)
            {
                this.clearRecipe();
            }
        }
    }

    public void copyRecipeFrom(RecipePattern other)
    {
        int size = other.getRecipeLength();
        ItemStack[] otherRecipe = other.getRecipeItems();

        this.ensureRecipeSizeAndClearRecipe(size);

        for (int i = 0; i < size; i++)
        {
            this.recipe[i] = InventoryUtils.isStackEmpty(otherRecipe[i]) == false ? otherRecipe[i].copy() : InventoryUtils.EMPTY_STACK;
        }

        this.result = InventoryUtils.isStackEmpty(other.getResult()) == false ? other.getResult().copy() : InventoryUtils.EMPTY_STACK;
        this.vanillaRecipe = other.vanillaRecipe;
    }

    public void readFromNBT(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registryManager)
    {
        if (nbt.contains("Result", Constants.NBT.TAG_COMPOUND) && nbt.contains("Ingredients", Constants.NBT.TAG_LIST))
        {
            NbtList tagIngredients = nbt.getList("Ingredients", Constants.NBT.TAG_COMPOUND);
            int count = tagIngredients.size();
            int length = nbt.getInt("Length");

            if (length > 0)
            {
                this.ensureRecipeSizeAndClearRecipe(length);
            }

            for (int i = 0; i < count; i++)
            {
                NbtCompound tag = tagIngredients.getCompound(i);
                int slot = tag.getInt("Slot");

                if (slot >= 0 && slot < this.recipe.length)
                {
                    this.recipe[slot] = ItemStack.fromNbt(tag);
                }
            }

            this.result = ItemStack.fromNbt(nbt.getCompound("Result"));
        }
    }

    @Nonnull
    public NbtCompound writeToNBT(@Nonnull DynamicRegistryManager registryManager)
    {
        NbtCompound nbt = new NbtCompound();

        if (this.isValid())
        {
            NbtCompound tag = this.result.writeNbt(new NbtCompound());

            nbt.putInt("Length", this.recipe.length);
            nbt.put("Result", tag);

            NbtList tagIngredients = new NbtList();

            for (int i = 0; i < this.recipe.length; i++)
            {
                if (this.recipe[i].isEmpty() == false && InventoryUtils.isStackEmpty(this.recipe[i]) == false)
                {
                    tag = new NbtCompound();
                    tag.copyFrom(this.recipe[i].writeNbt(new NbtCompound()));

                    tag.putInt("Slot", i);
                    tagIngredients.add(tag);
                }
            }

            nbt.put("Ingredients", tagIngredients);
        }

        return nbt;
    }

    public ItemStack getResult()
    {
        if (this.result.isEmpty() == false)
        {
            return this.result;
        }
        else
        {
            return InventoryUtils.EMPTY_STACK;
        }
    }

    public int getRecipeLength()
    {
        return this.recipe.length;
    }

    public ItemStack[] getRecipeItems()
    {
        return this.recipe;
    }

    public boolean isEmpty()
    {
        boolean empty = true;

        for (int i = 0; i < this.getRecipeLength(); i++)
        {
            if (!this.getRecipeItems()[i].isEmpty())
            {
                empty = false;
            }
        }

        return empty || this.getResult().isEmpty();
    }

    public int countRecipeItems()
    {
        int count = 0;

        for (ItemStack itemStack : this.recipe)
        {
            if (!itemStack.isEmpty())
            {
                count++;
            }
        }

        return count;
    }

    public boolean isValid()
    {
        return InventoryUtils.isStackEmpty(this.getResult()) == false;
    }

    @Nullable
    public Recipe<?> getVanillaRecipeEntry()
    {
        return this.vanillaRecipe;
    }

    @Nullable
    public CraftingRecipe getVanillaRecipe()
    {
        if (this.recipe == null)
        {
            return null;
        }

        return this.vanillaRecipe;
    }
}
