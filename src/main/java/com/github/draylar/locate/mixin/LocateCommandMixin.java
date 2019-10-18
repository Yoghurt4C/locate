package com.github.draylar.locate.mixin;

import com.github.draylar.locate.api.LocateRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.LocateCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

@Mixin(LocateCommand.class)
public abstract class LocateCommandMixin {

    @Shadow
    private static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        throw new AssertionError();
    }

    @Inject(method = "register", at = @At(value = "RETURN"))
    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo info) {
        List<String> locatableFeatures = LocateRegistry.getLocatableFeatures();

        locatableFeatures.forEach(featureName -> {
            dispatcher.register(literal("locate").requires(source -> source.hasPermissionLevel(2))
                    .then(literal(featureName).executes(ctx -> execute(ctx.getSource(), featureName))));
        });
    }

    private LocateCommandMixin() {
        // NO-OP
    }
}
