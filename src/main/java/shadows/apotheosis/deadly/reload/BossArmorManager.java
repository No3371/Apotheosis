package shadows.apotheosis.deadly.reload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.coremod.api.ASMAPI;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.util.GearSet;
import shadows.placebo.util.json.ItemAdapter;
import shadows.placebo.util.json.NBTAdapter;

public class BossArmorManager extends JsonReloadListener {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ItemStack.class, ItemAdapter.INSTANCE).registerTypeAdapter(CompoundNBT.class, NBTAdapter.INSTANCE).setFieldNamingStrategy(f -> f.getName().equals(ASMAPI.mapField("field_76292_a")) ? "weight" : f.getName()).create();

	public static final BossArmorManager INSTANCE = new BossArmorManager();

	protected final Map<ResourceLocation, GearSet> registry = new HashMap<>();
	protected final List<GearSet> sets = new ArrayList<>();
	private int weight = 0;

	public BossArmorManager() {
		super(GSON, "boss_gear");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager mgr, IProfiler profiler) {
		sets.clear();
		registry.clear();
		objects.forEach((id, obj) -> {
			try {
				register(id, GSON.fromJson(obj, GearSet.class));
			} catch (Exception e) {
				DeadlyModule.LOGGER.error("Failed to load boss armor set {}.", id.toString());
				e.printStackTrace();
			}
		});
		if (registry.isEmpty()) throw new RuntimeException("No Apotheosis Boss armor sets were registered.  At least one is required.");
		DeadlyModule.LOGGER.info("Registered {} boss gear sets.", sets.size());
		weight = WeightedRandom.getTotalWeight(sets);
	}

	protected void register(ResourceLocation id, GearSet set) {
		if (!registry.containsKey(id)) {
			set.setId(id);
			registry.put(id, set);
			sets.add(set);
		} else DeadlyModule.LOGGER.error("Attempted to register a boss gear set with name {}, but it already exists!", id);
	}

	public GearSet getRandomSet(Random rand, @Nullable List<ResourceLocation> permitted) {
		if (permitted == null || permitted.isEmpty()) return WeightedRandom.getRandomItem(rand, sets, weight);
		List<GearSet> valid = sets.stream().filter(e -> permitted.contains(e.getId())).collect(Collectors.toList());
		if (valid.isEmpty()) return WeightedRandom.getRandomItem(rand, sets, weight);
		return WeightedRandom.getRandomItem(rand, valid);
	}

}
