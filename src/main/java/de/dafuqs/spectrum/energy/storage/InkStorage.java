package de.dafuqs.spectrum.energy.storage;

import de.dafuqs.spectrum.energy.color.InkColor;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

/**
 * This interface defines that an object can
 * store pigment energy and how much
 **/
public interface InkStorage {
	
	// if the storage is able to store this kind of color
	boolean accepts(InkColor color);
	
	// returns the amount of energy that could not be added
	long addEnergy(InkColor color, long amount);
	
	// returns true if all energy could be drained successfully
	// boolean requestEnergy(Map<CMYKColor, Integer> colors);
	
	// returns requestedAmount of energy. Returns the amount of energy that could be drained
	// In contrast to requestEnergy this drains the energy up until 0, even if not requestedAmount of energy is stored
	long drainEnergy(InkColor color, long requestedAmount);
	
	// returns true if the energy could be drained successfully
	// if not enough energy is stored, the amount of stored energy remains unchanged
	boolean requestEnergy(InkColor color, long requestedAmount);
	
	// gets the amount of stored energy of that type
	long getEnergy(InkColor color);
	
	// returns all stored energy with amounts
	//Map<ICMYKColor, Integer> getEnergy();
	
	// gets the amount of energy that can be stored per individual color
	long getMaxPerColor();
	
	// gets the amount of energy that can be stored in total
	long getMaxTotal();
	
	// gets the amount of energy that is currently stored
	long getCurrentTotal();
	
	// returns true if no energy is stored
	boolean isEmpty();
	
	// returns true if the max total is reached
	boolean isFull();
	
	// fill up the storage with as much energy as possible
	void fillCompletely();
	
	void addTooltip(World world, List<Text> tooltip, TooltipContext context);
	
}