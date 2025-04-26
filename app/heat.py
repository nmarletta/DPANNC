import matplotlib.pyplot as plt
import numpy as np
import sys
import os
import csv
from io import StringIO

def plot_heatmap_from_custom_format(foldername, basename):
    folder = os.path.join("results", foldername)
    filepath = os.path.join(folder, f"{basename}.csv")

    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        sys.exit(1)

    # Read raw lines
    with open(filepath, "r") as f:
        raw_lines = f.read().splitlines()

    # Separate metadata lines at the bottom (starting with '#')
    metadata_lines = []
    while raw_lines and raw_lines[-1].strip().startswith("#"):
        metadata_lines.insert(0, raw_lines.pop().strip()[1:].strip())

    # Skip non-data headers
    while raw_lines and (not raw_lines[0].replace(".", "", 1).replace("-", "", 1).replace(",", "", 1).isdigit()):
        raw_lines.pop(0)

    # Parse CSV
    csv_text = "\n".join(raw_lines)
    reader = csv.reader(StringIO(csv_text))
    data_lines = list(reader)

    if not data_lines or len(data_lines[0]) < 2:
        print("CSV data format is not as expected.")
        sys.exit(1)

    # Parse x-axis (r values)
    x_labels = [float(x.strip()) for x in data_lines[0][1:]]
    y_labels = []
    data = []

    for row in data_lines[1:]:
        if len(row) < 2:
            continue
        try:
            y_labels.append(float(row[0]))
            data.append([float(cell) for cell in row[1:]])
        except ValueError:
            continue

    data = np.array(data)

    # Plotting with matplotlib (no seaborn)
    fig, ax = plt.subplots(figsize=(12, 6))
    cax = ax.imshow(data, aspect='auto', origin='lower',
                    extent=[min(x_labels), max(x_labels), min(y_labels), max(y_labels)],
                    vmin=0, vmax=1, cmap='viridis')

    ax.set_xticks(x_labels)
    ax.set_yticks(y_labels)
    ax.set_xticklabels([f"{x:.2f}" for x in x_labels], rotation=90)
    ax.set_yticklabels([f"{y:.2f}" for y in y_labels])
    ax.set_xlabel("Initial Distance (r)")
    ax.set_ylabel("Gamma (γ)")
    ax.set_title("Failure Rate of (1 ± γ) Distortion Bound")

    fig.colorbar(cax, label="Failure Rate")

    if metadata_lines:
        metadata_text = "\n".join(metadata_lines)
        plt.figtext(0.15, 0.06, metadata_text, wrap=True,
                    horizontalalignment='left', fontsize=8)
        plt.subplots_adjust(bottom=0.2)

    pdf_path = os.path.join(folder, f"{basename}.pdf")
    plt.savefig(pdf_path, bbox_inches="tight")
    print(f"Heatmap saved to {pdf_path}")

    plt.show()

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 plot_heatmap.py <foldername> <basename>")
        print("Example: python3 plot_heatmap.py nash nash12")
        sys.exit(1)

    folder_arg = sys.argv[1]
    file_arg = sys.argv[2]
    plot_heatmap_from_custom_format(folder_arg, file_arg)
