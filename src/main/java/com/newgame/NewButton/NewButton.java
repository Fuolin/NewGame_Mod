package com.newgame.NewButton;


import com.newgame.Config;
import com.newgame.NewGame;
import com.newgame.handler.Delete;
import com.newgame.handler.RePlace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = NewGame.MOD_ID, value = Dist.CLIENT)
public class NewButton {
    @SubscribeEvent
    public static void onTitleScreenInit(ScreenEvent.Init.Post event) {
        // 仅在标题屏幕添加按钮
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            // 计算按钮位置
            int buttonX = titleScreen.width / 2 - 100;
            int buttonY = titleScreen.height / 4 + 164;

            // 创建按钮
            Button customButton = Button.builder(
                            Component.translatable("button.yourmodid.custom"), // 按钮文本
                            pressable -> handleButtonClick(titleScreen)
                    )
                    .bounds(buttonX, buttonY, 200, 20) // 位置和大小
                    .build();
            event.addListener(customButton);
        }
    }

    public static void handleButtonClick(Screen parentScreen) {
        //获取minecraft实例
        Minecraft minecraft = Minecraft.getInstance();
        //确认界面
        minecraft.setScreen(new ConfirmationScreen(parentScreen));

    }


    public static class ConfirmationScreen extends Screen {
        private final Screen parentScreen;

        protected ConfirmationScreen(Screen parentScreen) {
            super(Component.translatable("new_game.confirmation.title"));
            this.parentScreen = parentScreen;
        }

        @Override
        protected void init() {
            super.init();

            int buttonWidth = 150;
            int buttonHeight = 20;
            int buttonY = this.height / 2 + 20;
            int centerX = this.width / 2;

            // 确认按钮
            this.addRenderableWidget(Button.builder(
                    Component.translatable("new_game.confirmation.confirm"),
                    button -> proceedWithDeletion()
            ).bounds(centerX - buttonWidth - 5, buttonY, buttonWidth, buttonHeight).build());

            // 取消按钮
            this.addRenderableWidget(Button.builder(
                    Component.translatable("new_game.confirmation.cancel"),
                    button -> Minecraft.getInstance().setScreen(parentScreen)
            ).bounds(centerX + 5, buttonY, buttonWidth, buttonHeight).build());
        }
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);

            int centerX = this.width / 2;

            guiGraphics.drawCenteredString(
                    this.font,
                    Component.translatable("new_game.confirmation.alert_text"),
                    centerX,
                    this.height / 2 - 60,
                    0xFF0000
            );
        }



        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            // 监听ESC键（键码256）
            if (keyCode == 256) { // ESC键
                Minecraft.getInstance().setScreen(parentScreen);
                return true; // 表示已处理此按键事件
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void proceedWithDeletion() {

            Minecraft minecraft = Minecraft.getInstance();

            //加载界面
            minecraft.setScreen(new Screen(Component.translatable("new_game.processing")) {});
            CompletableFuture.runAsync(() -> {
                try {
                    //获取根目录和需要删除的文件
                    String root = NewGame.getRootPath();
                    List<? extends String> pathList = Config.getDelPathList();

                    // 循环删除文件
                    if (!pathList.isEmpty()) {
                        for (String path : pathList) {
                            Delete.del(path, root);
                        }
                    }

                    // 替换文件
                    RePlace.place(root);

                    // 删除存档目录
                    Delete.del("saves"+ File.separator, root);

                } catch (Exception e) {
                    // 记录错误日志
                    NewGame.LOGGER.error(e.toString());
                } finally {
                    // 回到主线程打开创建世界界面
                    minecraft.execute(() -> {
                        // 确保当前屏幕是我们的加载界面才切换，避免覆盖用户操作
                        if (minecraft.screen != null && minecraft.screen.getTitle().getString().equals(Component.translatable("new_game.processing").getString())) {
                            openCreateWorldUI(minecraft, parentScreen);
                        }
                    });
                }
            });
        }
    }

    public static void openCreateWorldUI(Minecraft minecraft, @Nullable Screen parentScreen) {
        // 确保在 Minecraft主线程执行（GUI 操作必须在主线程，否则会崩溃）
        minecraft.execute(() -> CreateWorldScreen.openFresh(minecraft, parentScreen));
    }

}
