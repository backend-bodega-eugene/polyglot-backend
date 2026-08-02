namespace LifePlan
{
    partial class MainForm
    {
        private System.ComponentModel.IContainer components = null;

        protected override void Dispose(bool disposing)
        {
            if (disposing && components != null)
            {
                components.Dispose();
            }

            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        private void InitializeComponent()
        {
            headerPanel = new AntdUI.Panel();
            lblTitle = new AntdUI.Label();
            lblCurrentTime = new AntdUI.Label();

            progressPanel = new AntdUI.Panel();
            lblProgressTitle = new AntdUI.Label();
            lblProgressValue = new AntdUI.Label();
            progressToday = new ProgressBar();

            mainTabs = new TabControl();
            tabToday = new TabPage();
            tabSettings = new TabPage();
            tabHistory = new TabPage();

            todaySplitContainer = new SplitContainer();

            pendingContainer = new TableLayoutPanel();
            pendingHeaderPanel = new AntdUI.Panel();
            lblPendingTitle = new AntdUI.Label();
            pendingFlowPanel = new FlowLayoutPanel();

            completedContainer = new TableLayoutPanel();
            completedHeaderPanel = new AntdUI.Panel();
            lblCompletedTitle = new AntdUI.Label();
            completedFlowPanel = new FlowLayoutPanel();

            lblSettingsPlaceholder = new AntdUI.Label();
            lblHistoryPlaceholder = new AntdUI.Label();

            navigationPanel = new AntdUI.Panel();
            btnToday = new AntdUI.ButtonShadow();
            btnSettings = new AntdUI.ButtonShadow();
            btnHistory = new AntdUI.ButtonShadow();

            headerPanel.SuspendLayout();
            progressPanel.SuspendLayout();
            mainTabs.SuspendLayout();
            tabToday.SuspendLayout();
            tabSettings.SuspendLayout();
            tabHistory.SuspendLayout();

            ((System.ComponentModel.ISupportInitialize)todaySplitContainer).BeginInit();
            todaySplitContainer.Panel1.SuspendLayout();
            todaySplitContainer.Panel2.SuspendLayout();
            todaySplitContainer.SuspendLayout();

            pendingContainer.SuspendLayout();
            pendingHeaderPanel.SuspendLayout();
            completedContainer.SuspendLayout();
            completedHeaderPanel.SuspendLayout();
            navigationPanel.SuspendLayout();

            SuspendLayout();

            // headerPanel
            headerPanel.Controls.Add(lblCurrentTime);
            headerPanel.Controls.Add(lblTitle);
            headerPanel.Dock = DockStyle.Top;
            headerPanel.Location = new Point(0, 0);
            headerPanel.Name = "headerPanel";
            headerPanel.Padding = new Padding(24, 0, 24, 0);
            headerPanel.Size = new Size(1186, 64);
            headerPanel.TabIndex = 0;

            // lblTitle
            lblTitle.Dock = DockStyle.Left;
            lblTitle.Font = new Font("Microsoft YaHei UI", 18F, FontStyle.Bold);
            lblTitle.Location = new Point(24, 0);
            lblTitle.Name = "lblTitle";
            lblTitle.Size = new Size(260, 64);
            lblTitle.TabIndex = 0;
            lblTitle.Text = "LifePlan";
            lblTitle.TextAlign = ContentAlignment.MiddleLeft;

            // lblCurrentTime
            lblCurrentTime.Dock = DockStyle.Right;
            lblCurrentTime.Font = new Font("Microsoft YaHei UI", 11F);
            lblCurrentTime.Location = new Point(746, 0);
            lblCurrentTime.Name = "lblCurrentTime";
            lblCurrentTime.Size = new Size(416, 64);
            lblCurrentTime.TabIndex = 1;
            lblCurrentTime.Text = "2026年7月29日 星期三 14:30";
            lblCurrentTime.TextAlign = ContentAlignment.MiddleRight;

            // progressPanel
            progressPanel.Controls.Add(progressToday);
            progressPanel.Controls.Add(lblProgressValue);
            progressPanel.Controls.Add(lblProgressTitle);
            progressPanel.Dock = DockStyle.Top;
            progressPanel.Location = new Point(0, 64);
            progressPanel.Name = "progressPanel";
            progressPanel.Padding = new Padding(24, 15, 24, 15);
            progressPanel.Size = new Size(1186, 92);
            progressPanel.TabIndex = 1;

            // lblProgressTitle
            lblProgressTitle.Dock = DockStyle.Top;
            lblProgressTitle.Font = new Font("Microsoft YaHei UI", 11F, FontStyle.Bold);
            lblProgressTitle.Location = new Point(24, 15);
            lblProgressTitle.Name = "lblProgressTitle";
            lblProgressTitle.Size = new Size(1138, 28);
            lblProgressTitle.TabIndex = 0;
            lblProgressTitle.Text = "今日完成进度";

            // lblProgressValue
            lblProgressValue.Dock = DockStyle.Right;
            lblProgressValue.Font = new Font("Microsoft YaHei UI", 10F, FontStyle.Bold);
            lblProgressValue.Location = new Point(1022, 43);
            lblProgressValue.Name = "lblProgressValue";
            lblProgressValue.Size = new Size(140, 34);
            lblProgressValue.TabIndex = 2;
            lblProgressValue.Text = "2 / 5    40%";
            lblProgressValue.TextAlign = ContentAlignment.MiddleRight;

            // progressToday
            progressToday.Dock = DockStyle.Fill;
            progressToday.Location = new Point(24, 43);
            progressToday.Maximum = 100;
            progressToday.Name = "progressToday";
            progressToday.Size = new Size(998, 34);
            progressToday.Style = ProgressBarStyle.Continuous;
            progressToday.TabIndex = 1;
            progressToday.Value = 40;

            // mainTabs
            mainTabs.Appearance = TabAppearance.FlatButtons;
            mainTabs.Controls.Add(tabToday);
            mainTabs.Controls.Add(tabSettings);
            mainTabs.Controls.Add(tabHistory);
            mainTabs.Dock = DockStyle.Fill;
            mainTabs.ItemSize = new Size(0, 1);
            mainTabs.Location = new Point(0, 156);
            mainTabs.Name = "mainTabs";
            mainTabs.Padding = new Point(0, 0);
            mainTabs.SelectedIndex = 0;
            mainTabs.Size = new Size(1186, 628);
            mainTabs.SizeMode = TabSizeMode.Fixed;
            mainTabs.TabIndex = 2;

            // tabToday
            tabToday.Controls.Add(todaySplitContainer);
            tabToday.Location = new Point(4, 5);
            tabToday.Name = "tabToday";
            tabToday.Padding = new Padding(12);
            tabToday.Size = new Size(1178, 619);
            tabToday.TabIndex = 0;
            tabToday.Text = "今日";
            tabToday.UseVisualStyleBackColor = true;

            // todaySplitContainer
            todaySplitContainer.Dock = DockStyle.Fill;
            todaySplitContainer.Location = new Point(12, 12);
            todaySplitContainer.Name = "todaySplitContainer";
            todaySplitContainer.SplitterDistance = 566;
            todaySplitContainer.SplitterWidth = 12;
            todaySplitContainer.TabIndex = 0;

            // todaySplitContainer.Panel1
            todaySplitContainer.Panel1.Controls.Add(pendingContainer);

            // todaySplitContainer.Panel2
            todaySplitContainer.Panel2.Controls.Add(completedContainer);

            // pendingContainer
            pendingContainer.ColumnCount = 1;
            pendingContainer.ColumnStyles.Add(
                new ColumnStyle(SizeType.Percent, 100F));
            pendingContainer.Controls.Add(pendingHeaderPanel, 0, 0);
            pendingContainer.Controls.Add(pendingFlowPanel, 0, 1);
            pendingContainer.Dock = DockStyle.Fill;
            pendingContainer.RowCount = 2;
            pendingContainer.RowStyles.Add(new RowStyle(SizeType.Absolute, 56F));
            pendingContainer.RowStyles.Add(new RowStyle(SizeType.Percent, 100F));
            pendingContainer.TabIndex = 0;

            // pendingHeaderPanel
            pendingHeaderPanel.Controls.Add(lblPendingTitle);
            pendingHeaderPanel.Dock = DockStyle.Fill;
            pendingHeaderPanel.Location = new Point(3, 3);
            pendingHeaderPanel.Name = "pendingHeaderPanel";
            pendingHeaderPanel.Padding = new Padding(16, 0, 16, 0);
            pendingHeaderPanel.Size = new Size(560, 50);
            pendingHeaderPanel.TabIndex = 0;

            // lblPendingTitle
            lblPendingTitle.Dock = DockStyle.Fill;
            lblPendingTitle.Font = new Font(
                "Microsoft YaHei UI",
                13F,
                FontStyle.Bold);
            lblPendingTitle.ForeColor = Color.FromArgb(173, 104, 0);
            lblPendingTitle.Location = new Point(16, 0);
            lblPendingTitle.Name = "lblPendingTitle";
            lblPendingTitle.Size = new Size(528, 50);
            lblPendingTitle.TabIndex = 0;
            lblPendingTitle.Text = "待完成 / 进行中";
            lblPendingTitle.TextAlign = ContentAlignment.MiddleLeft;

            // pendingFlowPanel
            pendingFlowPanel.AutoScroll = true;
            pendingFlowPanel.BackColor = Color.FromArgb(250, 250, 250);
            pendingFlowPanel.Dock = DockStyle.Fill;
            pendingFlowPanel.FlowDirection = FlowDirection.TopDown;
            pendingFlowPanel.Location = new Point(3, 59);
            pendingFlowPanel.Name = "pendingFlowPanel";
            pendingFlowPanel.Padding = new Padding(8);
            pendingFlowPanel.Size = new Size(560, 545);
            pendingFlowPanel.TabIndex = 1;
            pendingFlowPanel.WrapContents = false;
            pendingFlowPanel.SizeChanged += PendingFlowPanel_SizeChanged;

            // completedContainer
            completedContainer.ColumnCount = 1;
            completedContainer.ColumnStyles.Add(
                new ColumnStyle(SizeType.Percent, 100F));
            completedContainer.Controls.Add(completedHeaderPanel, 0, 0);
            completedContainer.Controls.Add(completedFlowPanel, 0, 1);
            completedContainer.Dock = DockStyle.Fill;
            completedContainer.RowCount = 2;
            completedContainer.RowStyles.Add(new RowStyle(SizeType.Absolute, 56F));
            completedContainer.RowStyles.Add(new RowStyle(SizeType.Percent, 100F));
            completedContainer.TabIndex = 0;

            // completedHeaderPanel
            completedHeaderPanel.Controls.Add(lblCompletedTitle);
            completedHeaderPanel.Dock = DockStyle.Fill;
            completedHeaderPanel.Location = new Point(3, 3);
            completedHeaderPanel.Name = "completedHeaderPanel";
            completedHeaderPanel.Padding = new Padding(16, 0, 16, 0);
            completedHeaderPanel.Size = new Size(576, 50);
            completedHeaderPanel.TabIndex = 0;

            // lblCompletedTitle
            lblCompletedTitle.Dock = DockStyle.Fill;
            lblCompletedTitle.Font = new Font(
                "Microsoft YaHei UI",
                13F,
                FontStyle.Bold);
            lblCompletedTitle.ForeColor = Color.FromArgb(24, 144, 85);
            lblCompletedTitle.Location = new Point(16, 0);
            lblCompletedTitle.Name = "lblCompletedTitle";
            lblCompletedTitle.Size = new Size(544, 50);
            lblCompletedTitle.TabIndex = 0;
            lblCompletedTitle.Text = "已完成";
            lblCompletedTitle.TextAlign = ContentAlignment.MiddleLeft;

            // completedFlowPanel
            completedFlowPanel.AutoScroll = true;
            completedFlowPanel.BackColor = Color.FromArgb(250, 250, 250);
            completedFlowPanel.Dock = DockStyle.Fill;
            completedFlowPanel.FlowDirection = FlowDirection.TopDown;
            completedFlowPanel.Location = new Point(3, 59);
            completedFlowPanel.Name = "completedFlowPanel";
            completedFlowPanel.Padding = new Padding(8);
            completedFlowPanel.Size = new Size(576, 545);
            completedFlowPanel.TabIndex = 1;
            completedFlowPanel.WrapContents = false;
            completedFlowPanel.SizeChanged += CompletedFlowPanel_SizeChanged;

            // tabSettings
            tabSettings.Controls.Add(lblSettingsPlaceholder);
            tabSettings.Location = new Point(4, 5);
            tabSettings.Name = "tabSettings";
            tabSettings.Padding = new Padding(20);
            tabSettings.Size = new Size(1178, 619);
            tabSettings.TabIndex = 1;
            tabSettings.Text = "设置";
            tabSettings.UseVisualStyleBackColor = true;

            // lblSettingsPlaceholder
            lblSettingsPlaceholder.Dock = DockStyle.Fill;
            lblSettingsPlaceholder.Font = new Font(
                "Microsoft YaHei UI",
                18F,
                FontStyle.Bold);
            lblSettingsPlaceholder.Location = new Point(20, 20);
            lblSettingsPlaceholder.Name = "lblSettingsPlaceholder";
            lblSettingsPlaceholder.Size = new Size(1138, 579);
            lblSettingsPlaceholder.TabIndex = 0;
            lblSettingsPlaceholder.Text = "这里以后放任务模板管理";
            lblSettingsPlaceholder.TextAlign = ContentAlignment.MiddleCenter;

            // tabHistory
            tabHistory.Controls.Add(lblHistoryPlaceholder);
            tabHistory.Location = new Point(4, 5);
            tabHistory.Name = "tabHistory";
            tabHistory.Padding = new Padding(20);
            tabHistory.Size = new Size(1178, 619);
            tabHistory.TabIndex = 2;
            tabHistory.Text = "历史";
            tabHistory.UseVisualStyleBackColor = true;

            // lblHistoryPlaceholder
            lblHistoryPlaceholder.Dock = DockStyle.Fill;
            lblHistoryPlaceholder.Font = new Font(
                "Microsoft YaHei UI",
                18F,
                FontStyle.Bold);
            lblHistoryPlaceholder.Location = new Point(20, 20);
            lblHistoryPlaceholder.Name = "lblHistoryPlaceholder";
            lblHistoryPlaceholder.Size = new Size(1138, 579);
            lblHistoryPlaceholder.TabIndex = 0;
            lblHistoryPlaceholder.Text = "这里以后显示每日完成历史";
            lblHistoryPlaceholder.TextAlign = ContentAlignment.MiddleCenter;

            // navigationPanel
            navigationPanel.Controls.Add(btnHistory);
            navigationPanel.Controls.Add(btnSettings);
            navigationPanel.Controls.Add(btnToday);
            navigationPanel.Dock = DockStyle.Bottom;
            navigationPanel.Location = new Point(0, 784);
            navigationPanel.Name = "navigationPanel";
            navigationPanel.Padding = new Padding(16, 5, 16, 5);
            navigationPanel.Size = new Size(1186, 56);
            navigationPanel.TabIndex = 3;

            // btnToday
            btnToday.Dock = DockStyle.Left;
            btnToday.Font = new Font(
                "Microsoft YaHei UI",
                11F,
                FontStyle.Bold);
            btnToday.Location = new Point(16, 5);
            btnToday.Name = "btnToday";
            btnToday.Size = new Size(190, 46);
            btnToday.TabIndex = 0;
            btnToday.Text = "今日";
            btnToday.Click += BtnToday_Click;

            // btnSettings
            btnSettings.Dock = DockStyle.Left;
            btnSettings.Font = new Font(
                "Microsoft YaHei UI",
                11F,
                FontStyle.Bold);
            btnSettings.Location = new Point(206, 5);
            btnSettings.Name = "btnSettings";
            btnSettings.Size = new Size(190, 46);
            btnSettings.TabIndex = 1;
            btnSettings.Text = "模板设置";
            btnSettings.Click += BtnSettings_Click;

            // btnHistory
            btnHistory.Dock = DockStyle.Left;
            btnHistory.Font = new Font(
                "Microsoft YaHei UI",
                11F,
                FontStyle.Bold);
            btnHistory.Location = new Point(396, 5);
            btnHistory.Name = "btnHistory";
            btnHistory.Size = new Size(190, 46);
            btnHistory.TabIndex = 2;
            btnHistory.Text = "历史";
            btnHistory.Click += BtnHistory_Click;

            // MainForm
            AutoScaleDimensions = new SizeF(14F, 31F);
            AutoScaleMode = AutoScaleMode.Font;
            BackColor = Color.FromArgb(245, 247, 250);
            ClientSize = new Size(1186, 840);
            Controls.Add(mainTabs);
            Controls.Add(progressPanel);
            Controls.Add(headerPanel);
            Controls.Add(navigationPanel);
            MinimumSize = new Size(1000, 700);
            Name = "MainForm";
            StartPosition = FormStartPosition.CenterScreen;
            Text = "LifePlan";

            headerPanel.ResumeLayout(false);
            progressPanel.ResumeLayout(false);
            mainTabs.ResumeLayout(false);
            tabToday.ResumeLayout(false);
            tabSettings.ResumeLayout(false);
            tabHistory.ResumeLayout(false);

            todaySplitContainer.Panel1.ResumeLayout(false);
            todaySplitContainer.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)todaySplitContainer)
                .EndInit();
            todaySplitContainer.ResumeLayout(false);

            pendingContainer.ResumeLayout(false);
            pendingHeaderPanel.ResumeLayout(false);
            completedContainer.ResumeLayout(false);
            completedHeaderPanel.ResumeLayout(false);
            navigationPanel.ResumeLayout(false);

            ResumeLayout(false);
        }

        #endregion

        private AntdUI.Panel headerPanel;
        private AntdUI.Label lblTitle;
        private AntdUI.Label lblCurrentTime;

        private AntdUI.Panel progressPanel;
        private AntdUI.Label lblProgressTitle;
        private AntdUI.Label lblProgressValue;
        private ProgressBar progressToday;

        private TabControl mainTabs;
        private TabPage tabToday;
        private TabPage tabSettings;
        private TabPage tabHistory;

        private SplitContainer todaySplitContainer;

        private TableLayoutPanel pendingContainer;
        private AntdUI.Panel pendingHeaderPanel;
        private AntdUI.Label lblPendingTitle;
        private FlowLayoutPanel pendingFlowPanel;

        private TableLayoutPanel completedContainer;
        private AntdUI.Panel completedHeaderPanel;
        private AntdUI.Label lblCompletedTitle;
        private FlowLayoutPanel completedFlowPanel;

        private AntdUI.Label lblSettingsPlaceholder;
        private AntdUI.Label lblHistoryPlaceholder;

        private AntdUI.Panel navigationPanel;
        private AntdUI.ButtonShadow btnToday;
        private AntdUI.ButtonShadow btnSettings;
        private AntdUI.ButtonShadow btnHistory;
    }
}