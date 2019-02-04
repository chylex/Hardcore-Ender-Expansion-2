package chylex.hee.init.factory;
import chylex.hee.game.entity.item.EntityFallingBlockHeavy;
import chylex.hee.game.entity.item.EntityFallingObsidian;
import chylex.hee.game.entity.item.EntityInfusedTNT;
import chylex.hee.game.entity.item.EntityItemFreshlyCooked;
import chylex.hee.game.entity.item.EntityItemIgneousRock;
import chylex.hee.game.entity.item.EntityItemNoBob;
import chylex.hee.game.entity.living.EntityMobEndermite;
import chylex.hee.game.entity.living.EntityMobEndermiteInstability;
import chylex.hee.game.entity.living.EntityMobSilverfish;
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl;
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder;
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash;
import chylex.hee.game.entity.technical.EntityTechnicalTrigger;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class EntityConstructors{
	private static final Map<Class<? extends Entity>, Function<World, ? extends Entity>> all = new HashMap<>();
	
	static{
		add(EntityItemNoBob.class, EntityItemNoBob::new);
		add(EntityItemIgneousRock.class, EntityItemIgneousRock::new);
		add(EntityItemFreshlyCooked.class, EntityItemFreshlyCooked::new);
		
		add(EntityFallingBlockHeavy.class, EntityFallingBlockHeavy::new);
		add(EntityFallingObsidian.class, EntityFallingObsidian::new);
		add(EntityInfusedTNT.class, EntityInfusedTNT::new);
		
		add(EntityMobEndermite.class, EntityMobEndermite::new);
		add(EntityMobEndermiteInstability.class, EntityMobEndermiteInstability::new);
		add(EntityMobSilverfish.class, EntityMobSilverfish::new);
		
		add(EntityProjectileSpatialDash.class, EntityProjectileSpatialDash::new);
		add(EntityProjectileEyeOfEnder.class, EntityProjectileEyeOfEnder::new);
		add(EntityProjectileEnderPearl.class, EntityProjectileEnderPearl::new);
		
		add(EntityTechnicalTrigger.class, EntityTechnicalTrigger::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> Function<World, T> get(Class<T> cls){
		return (Function<World, T>)all.get(cls);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends Entity> void add(Class<T> cls, Function<World, T> constructor){
		all.put(cls, constructor);
	}
	
	private EntityConstructors(){}
}
