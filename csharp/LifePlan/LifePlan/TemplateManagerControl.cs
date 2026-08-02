using Model;
using Service;

namespace LifePlan;

public partial class TemplateManagerControl : UserControl
{
    private readonly TaskTemplateStore _store = new();

    private List<TaskTemplate> _templates = [];

    public event EventHandler? TemplatesChanged;

    public TemplateManagerControl()
    {
        InitializeComponent();

        gridTemplates.AutoGenerateColumns = false;

        LoadTemplates();
    }

    private void LoadTemplates()
    {
        _templates = _store.Load();

        if (_templates.Count == 0)
        {
            AddExampleTemplates();
        }

        BindGrid(_templates);
    }

    private void AddExampleTemplates()
    {
        _templates =
        [
            new TaskTemplate
            {
                Name = "英语学习",
                ReminderTime = new TimeOnly(10, 0),
                DurationMinutes = 45,
                Remark = "百词斩30个词，听力15分钟"
            },
            new TaskTemplate
            {
                Name = "拳击训练",
                ReminderTime = new TimeOnly(17, 30),
                DurationMinutes = 40,
                Remark = "拳击或其他有氧运动"
            },
            new TaskTemplate
            {
                Name = "吉他练习",
                ReminderTime = new TimeOnly(20, 0),
                DurationMinutes = 20,
                Remark = "和弦、节奏、完整歌曲"
            }
        ];

        _store.Save(_templates);
    }

    private void BindGrid(
        IEnumerable<TaskTemplate> templates)
    {
        var rows = templates
            .Select(x => new TaskTemplateRow
            {
                Id = x.Id,
                Name = x.Name,
                ReminderTimeText =
                    x.ReminderTime.ToString("HH:mm"),
                DurationText =
                    $"{x.DurationMinutes} 分钟",
                Remark = x.Remark ?? string.Empty
            })
            .ToList();

        gridTemplates.DataSource = null;
        gridTemplates.DataSource = rows;
    }

    private void BtnSearch_Click(
        object? sender,
        EventArgs e)
    {
        Search();
    }

    private void TxtKeyword_KeyDown(
        object? sender,
        KeyEventArgs e)
    {
        if (e.KeyCode != Keys.Enter)
        {
            return;
        }

        Search();

        e.SuppressKeyPress = true;
    }

    private void Search()
    {
        var keyword = txtKeyword.Text.Trim();

        if (string.IsNullOrWhiteSpace(keyword))
        {
            BindGrid(_templates);
            return;
        }

        var result = _templates
            .Where(x =>
                x.Name.Contains(
                    keyword,
                    StringComparison.OrdinalIgnoreCase)
                ||
                (x.Remark?.Contains(
                    keyword,
                    StringComparison.OrdinalIgnoreCase)
                 ?? false))
            .ToList();

        BindGrid(result);
    }

    private void BtnReset_Click(
        object? sender,
        EventArgs e)
    {
        txtKeyword.Clear();
        BindGrid(_templates);
    }

    private void BtnAdd_Click(
        object? sender,
        EventArgs e)
    {
        using var form = new TemplateEditForm();

        if (form.ShowDialog(this) != DialogResult.OK)
        {
            return;
        }

        _templates.Add(form.Template);

        SaveAndRefresh();
    }

    private void GridTemplates_CellContentClick(
        object? sender,
        DataGridViewCellEventArgs e)
    {
        if (e.RowIndex < 0)
        {
            return;
        }

        if (gridTemplates.Rows[e.RowIndex].DataBoundItem
            is not TaskTemplateRow row)
        {
            return;
        }

        if (e.ColumnIndex == colEdit.Index)
        {
            EditTemplate(row.Id);
            return;
        }

        if (e.ColumnIndex == colDelete.Index)
        {
            DeleteTemplate(row.Id);
        }
    }

    private void EditTemplate(Guid id)
    {
        var template = _templates
            .FirstOrDefault(x => x.Id == id);

        if (template == null)
        {
            return;
        }

        using var form = new TemplateEditForm(template);

        if (form.ShowDialog(this) != DialogResult.OK)
        {
            return;
        }

        SaveAndRefresh();
    }

    private void DeleteTemplate(Guid id)
    {
        var template = _templates
            .FirstOrDefault(x => x.Id == id);

        if (template == null)
        {
            return;
        }

        var result = MessageBox.Show(
            $"确定删除任务模板“{template.Name}”吗？",
            "删除确认",
            MessageBoxButtons.YesNo,
            MessageBoxIcon.Question);

        if (result != DialogResult.Yes)
        {
            return;
        }

        _templates.Remove(template);

        SaveAndRefresh();
    }

    private void SaveAndRefresh()
    {
        _store.Save(_templates);
        BindGrid(_templates);
        TemplatesChanged?.Invoke(this, EventArgs.Empty);
    }

    private sealed class TaskTemplateRow
    {
        public Guid Id { get; set; }

        public string Name { get; set; } = string.Empty;

        public string ReminderTimeText { get; set; } =
            string.Empty;

        public string DurationText { get; set; } =
            string.Empty;

        public string Remark { get; set; } =
            string.Empty;
    }
}