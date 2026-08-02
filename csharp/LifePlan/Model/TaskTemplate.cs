using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Model;

public class TaskTemplate
{
    public Guid Id { get; set; } = Guid.NewGuid();

    public string Name { get; set; } = string.Empty;

    public TimeOnly ReminderTime { get; set; }

    public int DurationMinutes { get; set; }

    public string? Remark { get; set; }
}
