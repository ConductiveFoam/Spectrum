package de.dafuqs.spectrum.recipe.spirit_instiller;

import com.google.gson.JsonObject;
import de.dafuqs.spectrum.blocks.spirit_instiller.SpiritInstillerBlock;
import de.dafuqs.spectrum.recipe.RecipeUtils;
import net.id.incubus_core.recipe.IngredientStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SpiritInstillerRecipeSerializer implements RecipeSerializer<SpiritInstillerRecipe> {
	
	public final SpiritInstillerRecipeSerializer.RecipeFactory<SpiritInstillerRecipe> recipeFactory;
	
	public SpiritInstillerRecipeSerializer(SpiritInstillerRecipeSerializer.RecipeFactory<SpiritInstillerRecipe> recipeFactory) {
		this.recipeFactory = recipeFactory;
	}
	
	@Override
	public SpiritInstillerRecipe read(Identifier identifier, JsonObject jsonObject) {
		String group = JsonHelper.getString(jsonObject, "group", "");
		IngredientStack ingredientStack1 = RecipeUtils.ingredientFromJson(JsonHelper.getObject(jsonObject, "ingredient1"));
		IngredientStack ingredientStack2 = RecipeUtils.ingredientFromJson(JsonHelper.getObject(jsonObject, "ingredient2"));
		IngredientStack centerIngredientStack = RecipeUtils.ingredientFromJson(JsonHelper.getObject(jsonObject, "center_ingredient"));
		ItemStack outputItemStack = RecipeUtils.itemStackWithNbtFromJson(JsonHelper.getObject(jsonObject, "result"));
		
		int craftingTime = JsonHelper.getInt(jsonObject, "time", 200);
		float experience = JsonHelper.getFloat(jsonObject, "experience");
		
		boolean noBenefitsFromYieldAndEfficiencyUpgrades = false;
		if (JsonHelper.hasPrimitive(jsonObject, "disable_yield_and_efficiency_upgrades")) {
			noBenefitsFromYieldAndEfficiencyUpgrades = JsonHelper.getBoolean(jsonObject, "disable_yield_and_efficiency_upgrades", false);
		}
		
		Identifier requiredAdvancementIdentifier;
		if (JsonHelper.hasString(jsonObject, "required_advancement")) {
			requiredAdvancementIdentifier = Identifier.tryParse(JsonHelper.getString(jsonObject, "required_advancement"));
		} else {
			// No unlock advancement set. Will be set to the unlock advancement of the block itself
			requiredAdvancementIdentifier = SpiritInstillerBlock.UNLOCK_IDENTIFIER;
		}
		
		return this.recipeFactory.create(identifier, group, ingredientStack1, ingredientStack2, centerIngredientStack, outputItemStack, craftingTime, experience, noBenefitsFromYieldAndEfficiencyUpgrades, requiredAdvancementIdentifier);
	}
	
	@Override
	public void write(PacketByteBuf packetByteBuf, SpiritInstillerRecipe spiritInstillerRecipe) {
		packetByteBuf.writeString(spiritInstillerRecipe.group);
		spiritInstillerRecipe.inputIngredient1.write(packetByteBuf);
		spiritInstillerRecipe.inputIngredient2.write(packetByteBuf);
		spiritInstillerRecipe.centerIngredient.write(packetByteBuf);
		packetByteBuf.writeItemStack(spiritInstillerRecipe.outputItemStack);
		packetByteBuf.writeInt(spiritInstillerRecipe.craftingTime);
		packetByteBuf.writeFloat(spiritInstillerRecipe.experience);
		packetByteBuf.writeBoolean(spiritInstillerRecipe.noBenefitsFromYieldAndEfficiencyUpgrades);
		packetByteBuf.writeIdentifier(spiritInstillerRecipe.requiredAdvancementIdentifier);
	}
	
	@Override
	public SpiritInstillerRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
		String group = packetByteBuf.readString();
		IngredientStack ingredient1 = IngredientStack.fromByteBuf(packetByteBuf);
		IngredientStack ingredient2 = IngredientStack.fromByteBuf(packetByteBuf);
		IngredientStack centerIngredient = IngredientStack.fromByteBuf(packetByteBuf);
		ItemStack outputItemStack = packetByteBuf.readItemStack();
		int craftingTime = packetByteBuf.readInt();
		float experience = packetByteBuf.readFloat();
		boolean noBenefitsFromYieldAndEfficiencyUpgrades = packetByteBuf.readBoolean();
		Identifier requiredAdvancementIdentifier = packetByteBuf.readIdentifier();
		
		return this.recipeFactory.create(identifier, group, ingredient1, ingredient2, centerIngredient, outputItemStack, craftingTime, experience, noBenefitsFromYieldAndEfficiencyUpgrades, requiredAdvancementIdentifier);
	}
	
	
	public interface RecipeFactory<SpiritInstillerRecipe> {
		SpiritInstillerRecipe create(Identifier id, String group, IngredientStack inputIngredient1, IngredientStack inputIngredient2, IngredientStack centerIngredient, ItemStack outputItemStack,
		                             int craftingTime, float experience, boolean noBenefitsFromYieldAndEfficiencyUpgrades, Identifier requiredAdvancementIdentifier);
	}
	
}
