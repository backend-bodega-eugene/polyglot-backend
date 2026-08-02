namespace Model;

public class TaskRecord
{
    public Guid Id { get; set; } = Guid.NewGuid();

    public Guid TemplateId { get; set; }

    public DateOnly Date { get; set; }

    public string Name { get; set; } = string.Empty;

    public TimeOnly ReminderTime { get; set; }

    public int DurationMinutes { get; set; }

    public string? Remark { get; set; }

    public LifeTaskStatus Status { get; set; } = LifeTaskStatus.Pending;

    public int? ActualDurationMinutes { get; set; }

    public string? CompletionRemark { get; set; }

    public DateTime? CompletedTime { get; set; }

    public DateTime UpdatedTime { get; set; } = DateTime.Now;
}
