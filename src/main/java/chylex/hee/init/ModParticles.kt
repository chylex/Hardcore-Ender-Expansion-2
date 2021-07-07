package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.particle.ParticleBubbleCustom
import chylex.hee.game.particle.ParticleCriticalHitCustom
import chylex.hee.game.particle.ParticleDeathFlowerHeal
import chylex.hee.game.particle.ParticleDust
import chylex.hee.game.particle.ParticleEnchantedHitCustom
import chylex.hee.game.particle.ParticleEnderGoo
import chylex.hee.game.particle.ParticleEnergyCluster
import chylex.hee.game.particle.ParticleEnergyClusterRevitalization
import chylex.hee.game.particle.ParticleEnergyTableDrain
import chylex.hee.game.particle.ParticleEnergyTransferToPedestal
import chylex.hee.game.particle.ParticleEnergyTransferToPlayer
import chylex.hee.game.particle.ParticleExperienceOrbFloating
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.ParticleGlitter
import chylex.hee.game.particle.ParticleGrowingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.ParticleSpellCustom
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.ParticleVoid
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.named
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.client.particle.IAnimatedSprite
import net.minecraft.client.particle.IParticleFactory
import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory
import net.minecraft.particles.BasicParticleType
import net.minecraft.particles.ParticleType
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeRunnable
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.registries.IForgeRegistry

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModParticles {
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<ParticleType<*>>) {
		with(e.registry) {
			register(ParticleBubbleCustom, "bubble")
			register(ParticleCriticalHitCustom, "critical_hit")
			register(ParticleDeathFlowerHeal, "death_flower_heal")
			register(ParticleDust, "dust")
			register(ParticleEnchantedHitCustom, "enchanted_hit")
			register(ParticleEnderGoo, "ender_goo")
			register(ParticleEnergyCluster, "energy_cluster")
			register(ParticleEnergyClusterRevitalization, "energy_cluster_revitalization")
			register(ParticleEnergyTableDrain, "energy_table_drain")
			register(ParticleEnergyTransferToPedestal, "energy_transfer_to_pedestal")
			register(ParticleEnergyTransferToPlayer, "energy_transfer_to_player")
			register(ParticleExperienceOrbFloating, "experience_orb_floating")
			register(ParticleFadingSpot, "fading_spot")
			register(ParticleFlameCustom, "flame")
			register(ParticleGlitter, "glitter")
			register(ParticleGrowingSpot, "growing_spot")
			register(ParticleSmokeCustom, "smoke")
			register(ParticleSpellCustom, "spell")
			register(ParticleTeleport, "teleport")
			register(ParticleVoid, "void")
		}
	}
	
	private fun IForgeRegistry<ParticleType<*>>.register(particle: IParticleMaker.WithData<*>, name: String) {
		val type = BasicParticleType(false)
		this.register(type named name)
		
		DistExecutor.safeRunWhenOn(Side.CLIENT) {
			SafeRunnable {
				Client.addFactory(type, particle) {
					particle.sprite = it
				}
			}
		}
	}
	
	@Sided(Side.CLIENT)
	@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID, bus = MOD)
	object Client {
		private val factories = mutableListOf<Runnable>()
		
		fun addFactory(type: BasicParticleType, maker: IParticleMaker<*>, callback: (IAnimatedSprite) -> Unit) {
			factories.add(Runnable {
				MC.particleManager.registerFactory(type, IParticleMetaFactory {
					callback(it)
					
					IParticleFactory { _, world, posX, posY, posZ, motX, motY, motZ ->
						maker.create(world, posX, posY, posZ, motX, motY, motZ, null)
					}
				})
			})
		}
		
		@SubscribeEvent
		fun onRegisterFactories(@Suppress("UNUSED_PARAMETER") e: ParticleFactoryRegisterEvent) {
			factories.forEach(Runnable::run)
			factories.clear()
		}
	}
}
