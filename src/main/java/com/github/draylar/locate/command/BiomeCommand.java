package com.github.draylar.locate.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;

import java.util.*;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.arguments.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BiomeCommand {
    private static final String COMMAND_LITERAL = "locatebiome";
    private static final String BIOME_ARGUMENT = "biome";
    private static final String DISTANCE_ARGUMENT = "distance";

    private static final int MAX_DISTANCE = 5000;
    private static final int DEFAULT_DISTANCE = 1000;

    private static final SuggestionProvider<ServerCommandSource> BIOME_SUGGESTIONS = SuggestionProviders.register(
            new Identifier("biomes"),
            (ctx, builder) -> {
                // get search phrase
                String searchPhrase = builder.getRemaining().toLowerCase(Locale.ROOT);

                // suggest each to builder
                Registry.BIOME.forEach(biome -> {
                    String biomeID = Objects.requireNonNull(Registry.BIOME.getId(biome)).toString();

                    if(searchPhrase.isEmpty()) {
                        builder.suggest(biomeID);
                    } else {
                        if(biomeID.contains(searchPhrase)) {
                            builder.suggest(biomeID);
                        }
                    }
                });

                // build future from builder
                return builder.buildFuture();
            }
    );

    private BiomeCommand() {
        // NO-OP
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(COMMAND_LITERAL)
                .then(argument(BIOME_ARGUMENT, identifier()).suggests(BIOME_SUGGESTIONS)
                        .then(argument(DISTANCE_ARGUMENT, integer(0, MAX_DISTANCE))
                                .executes(ctx -> executeFind(ctx.getSource(), getIdentifier(ctx, BIOME_ARGUMENT), getInteger(ctx, DISTANCE_ARGUMENT))))
                        .executes(ctx -> executeFind(ctx.getSource(), getIdentifier(ctx, BIOME_ARGUMENT), DEFAULT_DISTANCE))
                )
        );
    }

    private static int executeFind(ServerCommandSource source, Identifier biomeId, int range) throws CommandSyntaxException {
        Biome biome = Registry.BIOME.get(biomeId);
        verifyBiome(biomeId, biome);

        ServerWorld world = source.getWorld();
        BiomeSource biomeSource = world.getChunkManager().getChunkGenerator().getBiomeSource();

        BlockPos sourcePos = new BlockPos(source.getPosition());
        BlockPos biomePos = biomeSource.locateBiome(sourcePos.getX(), sourcePos.getZ(), range, Collections.singletonList(biome), new Random(world.getSeed()));

        if (biomePos == null) {
            source.getPlayer().sendMessage(new TranslatableText("biomeconquest.biome_not_found", new TranslatableText(biome.getTranslationKey()).asString(), range));
        } else {
            int distanceToBiome = MathHelper.floor(getDistance(sourcePos.getX(), sourcePos.getZ(), biomePos.getX(), biomePos.getZ()));

            Text teleportPrompt = Texts
                    .bracketed(new TranslatableText("chat.coordinates", biomePos.getX(), "~", biomePos.getZ()))
                    .styled(style -> style.setColor(Formatting.GREEN)
                            .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + biomePos.getX() + " " + 100 + " " + biomePos.getZ()))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")))
                    );

            source.sendFeedback(new TranslatableText("commands.locate.success", new TranslatableText(biome.getTranslationKey()), teleportPrompt, distanceToBiome), false);
        }

        return 1;
    }

    private static void verifyBiome(Identifier biomeId, Biome biome) throws CommandSyntaxException {
        if (biome == null) {
            throw new SimpleCommandExceptionType(new TranslatableText("biomeconquest.biome_not_valid", biomeId)).create();
        }
    }

    private static float getDistance(int x1, int y1, int x2, int y2) {
        int xDistance = x2 - x1;
        int yDistance = y2 - y1;

        return MathHelper.sqrt((float) (xDistance * xDistance + yDistance * yDistance));
    }
}
