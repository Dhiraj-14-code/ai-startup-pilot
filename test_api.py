import requests
import json
import sys

url = "http://127.0.0.1:8000/api/v1/analyze/file"
file_path = "d:/ai-startup-pilot/test_data/real_saas_startup_kpis.csv"

try:
    with open(file_path, "rb") as f:
        files = {"file": ("real_saas_startup_kpis.csv", f, "text/csv")}
        response = requests.post(url, files=files)
    
    if response.status_code == 200:
        data = response.json()
        print("Success! Health Score:", data.get("healthScore"))
        print("Status:", data.get("healthStatus"))
        print("\nPrediction:\n", data.get("prediction"))
        print("\nInsights:")
        for ins in data.get("insights", []):
            print(f"- {ins}")
            
        # Write the full response to a file for better formatting
        with open("d:/ai-startup-pilot/analysis_results.md", "w") as out:
            out.write("# 📊 AI Analysis Results: Real SaaS Startup\n\n")
            out.write(f"**Health Score**: {data.get('healthScore')}/100\n")
            out.write(f"**Health Status**: `{data.get('healthStatus')}`\n\n")
            out.write(f"### AI Prediction\n> {data.get('prediction')}\n\n")
            out.write(f"### Actionable Insights\n")
            for ins in data.get("insights", []):
                out.write(f"- {ins}\n")
            out.write("\n### KPI Trends\n")
            for trend in data.get("kpiTrends", []):
                out.write(f"- **{trend.get('metric')}**: {trend.get('direction')} ({trend.get('change_pct')}%)\n")
                
    else:
        print(f"Error: {response.status_code}")
        print(response.text)
except Exception as e:
    print(f"An error occurred: {e}")
