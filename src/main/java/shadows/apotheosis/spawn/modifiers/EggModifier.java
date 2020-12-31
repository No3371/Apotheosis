package shadows.apotheosis.spawn.modifiers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ResourceLocation;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;
import shadows.placebo.config.Configuration;
import shadows.placebo.util.SpawnerBuilder;

/**
 * Special case modifier to allow for spawn eggs to work properly.
 * @author Shadows
 *
 */
public class EggModifier extends SpawnerModifier {

	List<String> bannedMobs = new ArrayList<>();
	List<String> bannedNameSpaces = new ArrayList<>();

	public EggModifier(ItemStack item) {
		super(item, -1, -1, -1);
	}

	public EggModifier() {
		this(ItemStack.EMPTY);
	}

	@Override
	public boolean canModify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		return stack.getItem() instanceof SpawnEggItem;
	}

	@Override
	public boolean modify(ApothSpawnerTile spawner, ItemStack stack, boolean inverting) {
		ResourceLocation res = ((SpawnEggItem) stack.getItem()).getType(null).getRegistryName();
		if (bannedNameSpaces.contains(res.getNamespace().toString())) return false;
		String name = res.toString();
		if (bannedMobs.contains(name) || name.equals(spawner.spawnerLogic.spawnData.getNbt().getString(SpawnerBuilder.ID))) {
			spawner.spawnerLogic.potentialSpawns.clear();
			return false;
		}
		return true;
	}

	@Override
	public void load(Configuration cfg) {
		String[] bans = cfg.getStringList("Banned Mobs", getCategory(), new String[0], "A list of entity registry names that cannot be applied to spawners via egg.");
		for (String s : bans)
			bannedMobs.add(s);
		bans = cfg.getStringList("Banned Namespaces", getCategory(), new String[0], "A list of namespaces that all contained mobs cannot be applied to spawners via egg.");
		for (String s : bans)
			bannedNameSpaces.add(s);
	}

	@Override
	public String getCategory() {
		return "spawn_eggs";
	}

	@Override
	public String getDefaultItem() {
		return "";
	}

}