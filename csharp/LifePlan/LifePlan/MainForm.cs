using AntdUI;
using Model;
using Service;

namespace LifePlan;

public partial class MainForm : BaseForm
{
    private readonly System.Windows.Forms.Timer _clockTimer = new();

    private readonly System.Windows.Forms.Timer _dayCheckTimer = new();

    private DateOnly _loadedDate = DateOnly.FromDateTime(DateTime.Today);

    private readonly TaskTemplateStore _templateStore = new();

    private readonly TaskRecordStore _recordStore = new();

    private readonly FlowLayoutPanel _historyFlowPanel = new();

    private readonly Color _pendingBackground =
        Color.FromArgb(255, 248, 225);

    private readonly Color _pendingBorder =
        Color.FromArgb(255, 193, 7);

    private readonly Color _completedBackground =
        Color.FromArgb(232, 245, 233);

    private readonly Color _completedBorder =
        Color.FromArgb(46, 160, 67);

    public MainForm()
    {
        InitializeComponent();
        InitialiseHistoryPage();

        InitialiseClock();
        InitialiseDayCheck();
        LoadTodayTasks();
        UpdateProgress();
        SelectNavigation(0);
        LoadTemplateManager();
    }
    private void LoadTemplateManager()
    {
        tabSettings.Controls.Clear();

        var control = new TemplateManagerControl
        {
            Dock = DockStyle.Fill
        };

        control.TemplatesChanged += (_, _) => LoadTodayTasks();

        tabSettings.Controls.Add(control);
    }
    private void InitialiseClock()
    {
        UpdateCurrentTime();

        _clockTimer.Interval = 1000;
        _clockTimer.Tick += (_, _) => UpdateCurrentTime();
        _clockTimer.Start();
    }

    private void UpdateCurrentTime()
    {
        lblCurrentTime.Text =
            DateTime.Now.ToString("yyyy年M月d日 dddd HH:mm:ss");
    }

    private void InitialiseDayCheck()
    {
        _loadedDate = DateOnly.FromDateTime(DateTime.Today);

        _dayCheckTimer.Interval = 10 * 60 * 1000;
        _dayCheckTimer.Tick += (_, _) => CheckForNewDay();
        _dayCheckTimer.Start();
    }

    private void CheckForNewDay()
    {
        var today = DateOnly.FromDateTime(DateTime.Today);

        if (today <= _loadedDate)
        {
            return;
        }

        var templates = _templateStore.Load();

        // 如果电脑休眠或程序跨过了不止一天，逐日补齐记录。
        for (var date = _loadedDate; date < today; date = date.AddDays(1))
        {
            CompleteDateRecords(date, templates);
        }

        _loadedDate = today;
        LoadTodayTasks();
    }

    private void CompleteDateRecords(
        DateOnly date,
        List<TaskTemplate> templates)
    {
        var allRecords = _recordStore.Load();
        var dateRecords = allRecords
            .Where(x => x.Date == date)
            .ToList();

        var changed = false;

        foreach (var template in templates)
        {
            var exists = dateRecords.Any(x =>
                x.TemplateId == template.Id);

            if (exists)
            {
                continue;
            }

            allRecords.Add(new TaskRecord
            {
                TemplateId = template.Id,
                Date = date,
                Name = template.Name,
                ReminderTime = template.ReminderTime,
                DurationMinutes = template.DurationMinutes,
                Remark = template.Remark,
                Status = LifeTaskStatus.Pending,
                UpdatedTime = DateTime.Now
            });

            changed = true;
        }

        if (changed)
        {
            _recordStore.Save(allRecords);
        }
    }

    private void LoadTodayTasks()
    {
        pendingFlowPanel.Controls.Clear();
        completedFlowPanel.Controls.Clear();

        var templates = _templateStore
            .Load()
            .OrderBy(x => x.ReminderTime)
            .ToList();

        var records = SynchronizeTodayRecords(templates);

        foreach (var record in records
                     .OrderBy(x => x.ReminderTime))
        {
            var card = CreateTaskCard(record);

            if (record.Status == LifeTaskStatus.Completed)
            {
                completedFlowPanel.Controls.Add(card);
            }
            else
            {
                pendingFlowPanel.Controls.Add(card);
            }
        }

        ResizeTaskCards(pendingFlowPanel);
        ResizeTaskCards(completedFlowPanel);
        UpdateProgress();
    }

    private List<TaskRecord> SynchronizeTodayRecords(
        List<TaskTemplate> templates)
    {
        var today = DateOnly.FromDateTime(DateTime.Today);
        var allRecords = _recordStore.Load();
        var todayRecords = allRecords
            .Where(x => x.Date == today)
            .ToList();

        var templateIds = templates
            .Select(x => x.Id)
            .ToHashSet();

        // 已删除模板对应的今日未完成记录不再显示；已完成记录保留。
        allRecords.RemoveAll(x =>
            x.Date == today
            && x.Status != LifeTaskStatus.Completed
            && !templateIds.Contains(x.TemplateId));

        todayRecords = allRecords
            .Where(x => x.Date == today)
            .ToList();

        foreach (var template in templates)
        {
            var record = todayRecords
                .FirstOrDefault(x => x.TemplateId == template.Id);

            if (record == null)
            {
                record = new TaskRecord
                {
                    TemplateId = template.Id,
                    Date = today,
                    Name = template.Name,
                    ReminderTime = template.ReminderTime,
                    DurationMinutes = template.DurationMinutes,
                    Remark = template.Remark,
                    Status = LifeTaskStatus.Pending
                };

                allRecords.Add(record);
                todayRecords.Add(record);
                continue;
            }

            // 未完成记录随模板修改；已完成记录保留完成时的快照。
            if (record.Status != LifeTaskStatus.Completed)
            {
                record.Name = template.Name;
                record.ReminderTime = template.ReminderTime;
                record.DurationMinutes = template.DurationMinutes;
                record.Remark = template.Remark;
            }
        }

        _recordStore.Save(allRecords);

        return allRecords
            .Where(x => x.Date == today)
            .ToList();
    }

    private Control CreateTaskCard(TaskRecord record)
    {
        var completed = record.Status == LifeTaskStatus.Completed;
        var percentage = (int)record.Status;

        var background = completed
            ? _completedBackground
            : _pendingBackground;

        var border = completed
            ? _completedBorder
            : _pendingBorder;

        var card = new System.Windows.Forms.Panel
        {
            Height = 165,
            Margin = new Padding(4, 5, 4, 8),
            Padding = new Padding(16),
            BackColor = background,
            Tag = percentage
        };

        card.Paint += (_, e) =>
        {
            using var pen = new Pen(border, 2);
            var rect = card.ClientRectangle;
            rect.Width -= 1;
            rect.Height -= 1;
            e.Graphics.DrawRectangle(pen, rect);
        };

        var lblStatus = new System.Windows.Forms.Label
        {
            AutoSize = false,
            Dock = DockStyle.Right,
            Width = 100,
            Font = new Font(
                "Microsoft YaHei UI",
                10F,
                FontStyle.Bold),
            ForeColor = border,
            TextAlign = ContentAlignment.TopRight,
            Text = record.Status.GetDescription()
        };

        var lblTaskTitle = new System.Windows.Forms.Label
        {
            AutoSize = false,
            Dock = DockStyle.Top,
            Height = 46,
            Font = new Font(
                "Microsoft YaHei UI",
                13F,
                FontStyle.Bold),
            ForeColor = Color.FromArgb(45, 45, 45),
            Text = record.Name
        };

        var lblDetail = new System.Windows.Forms.Label
        {
            AutoSize = false,
            Dock = DockStyle.Top,
            Height = 48,
            Font = new Font(
                "Microsoft YaHei UI",
                9.5F),
            ForeColor = Color.FromArgb(100, 100, 100),
            TextAlign = ContentAlignment.MiddleLeft,
            Text = BuildRecordDetail(record)
        };

        var bottomPanel = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Bottom,
            Height = 44,
            BackColor = background
        };

        var btnOperation = new System.Windows.Forms.Button
        {
            Dock = DockStyle.Right,
            Width = 110,
            FlatStyle = FlatStyle.Flat,
            BackColor = Color.White,
            ForeColor = border,
            Text = "操作"
        };

        btnOperation.FlatAppearance.BorderColor = border;
        btnOperation.Click += (_, _) => OpenTaskOperation(record);

        var lblPercent = new System.Windows.Forms.Label
        {
            Dock = DockStyle.Fill,
            Font = new Font(
                "Microsoft YaHei UI",
                9.5F),
            ForeColor = border,
            TextAlign = ContentAlignment.MiddleLeft,
            Text = $"完成比例：{percentage}%"
        };

        bottomPanel.Controls.Add(btnOperation);
        bottomPanel.Controls.Add(lblPercent);

        card.Controls.Add(lblDetail);
        card.Controls.Add(lblTaskTitle);
        card.Controls.Add(lblStatus);
        card.Controls.Add(bottomPanel);

        return card;
    }

    private static string BuildRecordDetail(TaskRecord record)
    {
        var parts = new List<string>
        {
            $"计划 {record.DurationMinutes} 分钟",
            $"提醒时间 {record.ReminderTime:HH:mm}"
        };

        if (record.ActualDurationMinutes.HasValue)
        {
            parts.Add($"实际 {record.ActualDurationMinutes.Value} 分钟");
        }

        if (!string.IsNullOrWhiteSpace(record.CompletionRemark))
        {
            parts.Add(record.CompletionRemark);
        }
        else if (!string.IsNullOrWhiteSpace(record.Remark))
        {
            parts.Add(record.Remark);
        }

        return string.Join(" · ", parts);
    }

    private void OpenTaskOperation(TaskRecord record)
    {
        using var form = new TaskOperationForm(record);

        if (form.ShowDialog(this) != DialogResult.OK)
        {
            return;
        }

        _recordStore.Upsert(form.Record);
        LoadTodayTasks();
        LoadHistoryCards();
    }

    private void UpdateProgress()
    {
        var allCards =
            pendingFlowPanel.Controls
                .Cast<Control>()
                .Concat(
                    completedFlowPanel.Controls
                        .Cast<Control>())
                .ToList();

        var totalCount = allCards.Count;

        var completedCount =
            completedFlowPanel.Controls.Count;

        var percentage = totalCount == 0
            ? 0
            : completedCount * 100 / totalCount;

        progressToday.Value =
            Math.Clamp(percentage, 0, 100);

        lblProgressValue.Text =
            $"{completedCount} / {totalCount}    {percentage}%";
    }

    private void BtnToday_Click(
        object? sender,
        EventArgs e)
    {
        mainTabs.SelectedTab = tabToday;
        LoadTodayTasks();
        SelectNavigation(0);
    }

    private void BtnSettings_Click(
        object? sender,
        EventArgs e)
    {
        mainTabs.SelectedTab = tabSettings;
        SelectNavigation(1);
    }

    private void BtnHistory_Click(
        object? sender,
        EventArgs e)
    {
        mainTabs.SelectedTab = tabHistory;
        LoadHistoryCards();
        SelectNavigation(2);
    }


    private void InitialiseHistoryPage()
    {
        tabHistory.Controls.Clear();

        _historyFlowPanel.Dock = DockStyle.Fill;
        _historyFlowPanel.AutoScroll = true;
        _historyFlowPanel.FlowDirection = FlowDirection.TopDown;
        _historyFlowPanel.WrapContents = false;
        _historyFlowPanel.Padding = new Padding(16);
        _historyFlowPanel.BackColor = Color.FromArgb(245, 247, 250);
        _historyFlowPanel.SizeChanged += (_, _) =>
            ResizeHistoryDayPanels();

        tabHistory.Controls.Add(_historyFlowPanel);
    }

    private void LoadHistoryCards()
    {
        if (_historyFlowPanel.IsDisposed)
        {
            return;
        }

        _historyFlowPanel.SuspendLayout();

        try
        {
            _historyFlowPanel.Controls.Clear();

            var records = _recordStore
                .Load()
                .OrderByDescending(x => x.Date)
                .ThenBy(x => x.ReminderTime)
                .ToList();

            if (records.Count == 0)
            {
                _historyFlowPanel.Controls.Add(
                    CreateEmptyHistoryLabel());
                return;
            }

            foreach (var dateGroup in records.GroupBy(x => x.Date))
            {
                _historyFlowPanel.Controls.Add(
                    CreateHistoryDayPanel(
                        dateGroup.Key,
                        dateGroup.ToList()));
            }

            ResizeHistoryDayPanels();
        }
        finally
        {
            _historyFlowPanel.ResumeLayout();
        }
    }

    private Control CreateEmptyHistoryLabel()
    {
        return new System.Windows.Forms.Label
        {
            AutoSize = false,
            Height = 80,
            Width = 500,
            Margin = new Padding(4, 20, 4, 4),
            Font = new Font(
                "Microsoft YaHei UI",
                11F),
            ForeColor = Color.FromArgb(120, 120, 120),
            TextAlign = ContentAlignment.MiddleCenter,
            Text = "还没有历史记录"
        };
    }

    private Control CreateHistoryDayPanel(
        DateOnly date,
        List<TaskRecord> records)
    {
        var completedCount = records.Count(x =>
            x.Status == LifeTaskStatus.Completed);

        var totalProgress = records.Count == 0
            ? 0
            : (int)Math.Round(
                records.Average(x => (int)x.Status));

        var bodyHeight = records.Count * 104;
        var panelHeight = 62 + bodyHeight + 12;

        var dayPanel = new System.Windows.Forms.Panel
        {
            Height = panelHeight,
            Margin = new Padding(4, 4, 4, 14),
            Padding = new Padding(1),
            BackColor = Color.FromArgb(215, 220, 226)
        };

        var contentPanel = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Fill,
            BackColor = Color.White
        };

        var headerPanel = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Top,
            Height = 62,
            Padding = new Padding(16, 0, 16, 0),
            BackColor = Color.FromArgb(238, 242, 247)
        };

        var lblSummary = new System.Windows.Forms.Label
        {
            Dock = DockStyle.Right,
            Width = 230,
            Font = new Font(
                "Microsoft YaHei UI",
                9.5F,
                FontStyle.Bold),
            ForeColor = Color.FromArgb(80, 90, 105),
            TextAlign = ContentAlignment.MiddleRight,
            Text =
                $"完成 {completedCount}/{records.Count}    整体 {totalProgress}%"
        };

        var lblDate = new System.Windows.Forms.Label
        {
            Dock = DockStyle.Fill,
            Font = new Font(
                "Microsoft YaHei UI",
                12F,
                FontStyle.Bold),
            ForeColor = Color.FromArgb(45, 55, 70),
            TextAlign = ContentAlignment.MiddleLeft,
            Text = date.ToDateTime(TimeOnly.MinValue)
                .ToString("yyyy年M月d日  dddd")
        };

        headerPanel.Controls.Add(lblDate);
        headerPanel.Controls.Add(lblSummary);

        var taskContainer = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Fill,
            Padding = new Padding(0, 0, 0, 8),
            BackColor = Color.White
        };

        foreach (var record in records
                     .OrderByDescending(x => x.ReminderTime))
        {
            taskContainer.Controls.Add(
                CreateHistoryTaskRow(record));
        }

        contentPanel.Controls.Add(taskContainer);
        contentPanel.Controls.Add(headerPanel);
        dayPanel.Controls.Add(contentPanel);

        return dayPanel;
    }

    private Control CreateHistoryTaskRow(TaskRecord record)
    {
        var completed =
            record.Status == LifeTaskStatus.Completed;

        var statusColour = completed
            ? _completedBorder
            : _pendingBorder;

        var row = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Top,
            Height = 104,
            Padding = new Padding(16, 8, 16, 8),
            BackColor = Color.White
        };

        row.Paint += (_, e) =>
        {
            using var pen = new Pen(
                Color.FromArgb(232, 235, 239));

            e.Graphics.DrawLine(
                pen,
                16,
                row.Height - 1,
                row.Width - 16,
                row.Height - 1);
        };

        var rightPanel = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Right,
            Width = 210,
            BackColor = Color.White
        };

        var btnOperation = new System.Windows.Forms.Button
        {
            Dock = DockStyle.Right,
            Width = 90,
            Margin = new Padding(8),
            FlatStyle = FlatStyle.Flat,
            BackColor = Color.White,
            ForeColor = statusColour,
            Text = "操作"
        };

        btnOperation.FlatAppearance.BorderColor =
            statusColour;

        btnOperation.Click += (_, _) =>
            OpenTaskOperation(record);

        var lblStatus = new System.Windows.Forms.Label
        {
            Dock = DockStyle.Fill,
            Font = new Font(
                "Microsoft YaHei UI",
                9.5F,
                FontStyle.Bold),
            ForeColor = statusColour,
            TextAlign = ContentAlignment.MiddleRight,
            Text =
                $"{record.Status.GetDescription()}  {(int)record.Status}%"
        };

        rightPanel.Controls.Add(lblStatus);
        rightPanel.Controls.Add(btnOperation);

        var infoPanel = new System.Windows.Forms.Panel
        {
            Dock = DockStyle.Fill,
            BackColor = Color.White
        };

        var lblDetail = new System.Windows.Forms.Label
        {
            Dock = DockStyle.Fill,
            Font = new Font(
                "Microsoft YaHei UI",
                9.2F),
            ForeColor = Color.FromArgb(105, 105, 105),
            TextAlign = ContentAlignment.MiddleLeft,
            Text = BuildRecordDetail(record)
        };

        var lblName = new System.Windows.Forms.Label
        {
            Dock = DockStyle.Top,
            Height = 34,
            Font = new Font(
                "Microsoft YaHei UI",
                11F,
                FontStyle.Bold),
            ForeColor = Color.FromArgb(50, 50, 50),
            TextAlign = ContentAlignment.MiddleLeft,
            Text = record.Name
        };

        infoPanel.Controls.Add(lblDetail);
        infoPanel.Controls.Add(lblName);

        row.Controls.Add(infoPanel);
        row.Controls.Add(rightPanel);

        return row;
    }

    private void ResizeHistoryDayPanels()
    {
        var width = _historyFlowPanel.ClientSize.Width
                    - _historyFlowPanel.Padding.Horizontal
                    - SystemInformation.VerticalScrollBarWidth
                    - 12;

        foreach (Control control in
                 _historyFlowPanel.Controls)
        {
            control.Width = Math.Max(width, 320);
        }
    }

    private void SelectNavigation(int selectedIndex)
    {
        btnToday.Font = new Font(
            btnToday.Font,
            selectedIndex == 0
                ? FontStyle.Bold
                : FontStyle.Regular);

        btnSettings.Font = new Font(
            btnSettings.Font,
            selectedIndex == 1
                ? FontStyle.Bold
                : FontStyle.Regular);

        btnHistory.Font = new Font(
            btnHistory.Font,
            selectedIndex == 2
                ? FontStyle.Bold
                : FontStyle.Regular);
    }

    private void PendingFlowPanel_SizeChanged(
        object? sender,
        EventArgs e)
    {
        ResizeTaskCards(pendingFlowPanel);
    }

    private void CompletedFlowPanel_SizeChanged(
        object? sender,
        EventArgs e)
    {
        ResizeTaskCards(completedFlowPanel);
    }

    private static void ResizeTaskCards(
        FlowLayoutPanel container)
    {
        var width = container.ClientSize.Width
                    - container.Padding.Horizontal
                    - SystemInformation.VerticalScrollBarWidth
                    - 12;

        foreach (Control control in container.Controls)
        {
            control.Width = Math.Max(width, 200);
        }
    }

    protected override void OnFormClosed(
        FormClosedEventArgs e)
    {
        _clockTimer.Stop();
        _clockTimer.Dispose();

        _dayCheckTimer.Stop();
        _dayCheckTimer.Dispose();

        base.OnFormClosed(e);
    }
}