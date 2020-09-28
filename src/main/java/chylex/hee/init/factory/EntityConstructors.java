package chylex.hee.init.factory;
import chylex.hee.game.entity.effect.EntityTerritoryLightningBolt;
import chylex.hee.game.entity.item.EntityFallingBlockHeavy;
import chylex.hee.game.entity.item.EntityFallingObsidian;
import chylex.hee.game.entity.item.EntityInfusedTNT;
import chylex.hee.game.entity.item.EntityItemCauldronTrigger;
import chylex.hee.game.entity.item.EntityItemFreshlyCooked;
import chylex.hee.game.entity.item.EntityItemIgneousRock;
import chylex.hee.game.entity.item.EntityItemNoBob;
import chylex.hee.game.entity.item.EntityItemRevitalizationSubstance;
import chylex.hee.game.entity.item.EntityTokenHolder;
import chylex.hee.game.entity.living.EntityBossEnderEye;
import chylex.hee.game.entity.living.EntityMobAngryEnderman;
import chylex.hee.game.entity.living.EntityMobBlobby;
import chylex.hee.game.entity.living.EntityMobEnderman;
import chylex.hee.game.entity.living.EntityMobEndermanMuppet;
import chylex.hee.game.entity.living.EntityMobEndermite;
import chylex.hee.game.entity.living.EntityMobEndermiteInstability;
import chylex.hee.game.entity.living.EntityMobSilverfish;
import chylex.hee.game.entity.living.EntityMobSpiderling;
import chylex.hee.game.entity.living.EntityMobUndread;
import chylex.hee.game.entity.living.EntityMobVampireBat;
import chylex.hee.game.entity.living.EntityMobVillagerDying;
import chylex.hee.game.entity.projectile.EntityProjectileEnderPearl;
import chylex.hee.game.entity.projectile.EntityProjectileExperienceBottle;
import chylex.hee.game.entity.projectile.EntityProjectileEyeOfEnder;
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash;
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent;
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic;
import chylex.hee.game.entity.technical.EntityTechnicalPuzzle;
import chylex.hee.game.entity.technical.EntityTechnicalTrigger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import java.util.HashMap;
import java.util.Map;

public final class EntityConstructors{
	private static final Map<Class<? extends Entity>, EntityType.IFactory<? extends Entity>> all = new HashMap<>();
	
	static{
		add(EntityTerritoryLightningBolt.class, EntityTerritoryLightningBolt::new);
		
		add(EntityItemCauldronTrigger.class, EntityItemCauldronTrigger::new);
		add(EntityItemFreshlyCooked.class, EntityItemFreshlyCooked::new);
		add(EntityItemIgneousRock.class, EntityItemIgneousRock::new);
		add(EntityItemNoBob.class, EntityItemNoBob::new);
		add(EntityItemRevitalizationSubstance.class, EntityItemRevitalizationSubstance::new);
		
		add(EntityFallingBlockHeavy.class, EntityFallingBlockHeavy::new);
		add(EntityFallingObsidian.class, EntityFallingObsidian::new);
		add(EntityInfusedTNT.class, EntityInfusedTNT::new);
		add(EntityTokenHolder.class, EntityTokenHolder::new);
		
		add(EntityBossEnderEye.class, EntityBossEnderEye::new);
		
		add(EntityMobAngryEnderman.class, EntityMobAngryEnderman::new);
		add(EntityMobBlobby.class, EntityMobBlobby::new);
		add(EntityMobEnderman.class, EntityMobEnderman::new);
		add(EntityMobEndermanMuppet.class, EntityMobEndermanMuppet::new);
		add(EntityMobEndermite.class, EntityMobEndermite::new);
		add(EntityMobEndermiteInstability.class, EntityMobEndermiteInstability::new);
		add(EntityMobSilverfish.class, EntityMobSilverfish::new);
		add(EntityMobSpiderling.class, EntityMobSpiderling::new);
		add(EntityMobUndread.class, EntityMobUndread::new);
		add(EntityMobVampireBat.class, EntityMobVampireBat::new);
		add(EntityMobVillagerDying.class, EntityMobVillagerDying::new);
		
		add(EntityProjectileEnderPearl.class, EntityProjectileEnderPearl::new);
		add(EntityProjectileExperienceBottle.class, EntityProjectileExperienceBottle::new);
		add(EntityProjectileEyeOfEnder.class, EntityProjectileEyeOfEnder::new);
		add(EntityProjectileSpatialDash.class, EntityProjectileSpatialDash::new);
		
		add(EntityTechnicalCausatumEvent.class, EntityTechnicalCausatumEvent::new);
		add(EntityTechnicalIgneousPlateLogic.class, EntityTechnicalIgneousPlateLogic::new);
		add(EntityTechnicalPuzzle.class, EntityTechnicalPuzzle::new);
		add(EntityTechnicalTrigger.class, EntityTechnicalTrigger::new);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> EntityType.IFactory<T> get(final Class<T> cls){
		return (EntityType.IFactory<T>)all.get(cls);
	}
	
	/**
	 * Ensures the class and constructor are compatible to catch typos.
	 */
	private static <T extends Entity> void add(final Class<T> cls, final EntityType.IFactory<T> constructor){
		all.put(cls, constructor);
	}
	
	private EntityConstructors(){}
}
