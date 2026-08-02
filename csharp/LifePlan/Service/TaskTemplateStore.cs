using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using System.Text.Json;
using Model;

namespace Service;

public class TaskTemplateStore
{
    private readonly string _filePath;

    private readonly JsonSerializerOptions _options = new()
    {
        WriteIndented = true
    };

    public TaskTemplateStore()
    {
        var dataDirectory = Path.Combine(
            AppContext.BaseDirectory,
            "Data");

        Directory.CreateDirectory(dataDirectory);

        _filePath = Path.Combine(
            dataDirectory,
            "templates.json");
    }

    public List<TaskTemplate> Load()
    {
        if (!File.Exists(_filePath))
        {
            return [];
        }

        var json = File.ReadAllText(_filePath);

        if (string.IsNullOrWhiteSpace(json))
        {
            return [];
        }

        return JsonSerializer.Deserialize<List<TaskTemplate>>(
                   json,
                   _options)
               ?? [];
    }

    public void Save(List<TaskTemplate> templates)
    {
        var json = JsonSerializer.Serialize(
            templates,
            _options);

        File.WriteAllText(_filePath, json);
    }
}