package com.github.alexmodguy.alexscaves.client.model.layered;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class DivingArmorModel extends HumanoidModel {

    public DivingArmorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createArmorLayer(CubeDeformation deformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.getChild("head");
        PartDefinition body = partdefinition.getChild("body");
        PartDefinition leftLeg = partdefinition.getChild("left_leg");
        PartDefinition rightLeg = partdefinition.getChild("right_leg");
        PartDefinition leftArm = partdefinition.getChild("left_arm");
        PartDefinition rightArm = partdefinition.getChild("right_arm");
        // Neutralize the inherited vanilla "hat" overlay. HumanoidModel.createMesh adds an inflated
        // (deformation.extend(0.5)) opaque "hat" box as a CHILD OF head; its opaque bronze front face sits
        // at z=-5 — directly in front of (and coplanar with) the visor porthole/plate — so it paints a solid
        // bronze disc over the porthole and z-fights the visor. AC meant to zero it out but added the empty
        // "hat" to the ROOT partdefinition instead of head, leaving the real one intact. Target head here.
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(0, 32).addBox(4.0F, -5.0F, -3.0F, 2.0F, 4.0F, 4.0F, deformation)
                .texOffs(0, 32).mirror().addBox(-6.0F, -5.0F, -3.0F, 2.0F, 4.0F, 4.0F, deformation).mirror(false)
                .texOffs(24, 0).addBox(-3.0F, -7.01F, -5.0F, 6.0F, 6.0F, 2.0F, deformation)
                .texOffs(14, 37).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 2.0F, 9.0F, deformation), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("chestplate", CubeListBuilder.create()
                .texOffs(40, 4).addBox(-2.0F, 2.0F, 2.0F, 4.0F, 8.0F, 4.0F, deformation), PartPose.offset(0.0F, 0.0F, 0.0F));

        leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.35F)), PartPose.offset(1.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(48, 48).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.35F)), PartPose.offset(-1.0F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(32, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.35F)), PartPose.offset(0.0F, 0.75F, 0.0F));
        rightLeg.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(32, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.35F)), PartPose.offset(0.0F, 0.75F, 0.0F));
        body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation.extend(0.35F)), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
