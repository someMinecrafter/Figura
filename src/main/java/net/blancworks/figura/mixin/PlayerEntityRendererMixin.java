package net.blancworks.figura.mixin;

import net.blancworks.figura.PlayerData;
import net.blancworks.figura.models.CustomModelPart;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.blancworks.figura.FiguraMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        FiguraMod.setRenderingMode(abstractClientPlayerEntity, vertexConsumerProvider, ((PlayerEntityRenderer) (Object) this).getModel(), g);
    }

    @Inject(at = @At("TAIL"), method = "renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;)V", cancellable = true)
    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo info) {
        FiguraMod.setRenderingMode(player, vertexConsumers, ((PlayerEntityRenderer) (Object) this).getModel(), 0);
        PlayerData playerData = FiguraMod.getCurrData();

        if (playerData != null) {
            if (playerData.model != null) {
                if (playerData.texture == null || playerData.texture.ready == false) {
                    return;
                }
                //We actually wanna use this custom vertex consumer, not the one provided by the render arguments.
                VertexConsumer actualConsumer = FiguraMod.vertex_consumer_provider.getBuffer(RenderLayer.getEntityCutout(playerData.texture.id));

                PlayerEntityRenderer realRenderer = (PlayerEntityRenderer)(Object)this;
                PlayerEntityModel model = realRenderer.getModel();
                VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(playerData.texture.id));
                
                for (CustomModelPart part : playerData.model.all_parts) {
                    if(part.parentType == CustomModelPart.ParentType.RightArm && arm == model.rightArm){
                        matrices.push();
                        
                        model.rightArm.rotate(matrices);
                        part.render(99999, matrices, vc, light, OverlayTexture.DEFAULT_UV);
                        
                        matrices.pop();
                    } else if(part.parentType == CustomModelPart.ParentType.LeftArm && arm == model.leftArm){
                        matrices.push();

                        model.leftArm.rotate(matrices);
                        part.render(99999, matrices, vc, light, OverlayTexture.DEFAULT_UV);

                        matrices.pop();
                    }
                }
                
            }
        }
    }
}