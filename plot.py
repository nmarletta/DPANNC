import matplotlib.pyplot as plt
import sys
import os
import csv
from io import StringIO

def plot_from_custom_format(foldername, basename):
    folder = os.path.join("app/results", foldername)
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

    # Parse remaining CSV content
    csv_text = "\n".join(raw_lines)
    reader = csv.reader(StringIO(csv_text))
    lines = list(reader)

    if len(lines) < 4:
        raise ValueError("File format is incorrect or missing metadata lines.")

    # Parse plot config
    title = lines[0][0]
    x_index = int(lines[1][0])
    y_indices = list(map(int, lines[2]))
    headers = lines[3]

    all_indices = [x_index] + y_indices
    if any(i >= len(headers) for i in all_indices):
        raise IndexError("Index in metadata exceeds number of available columns")

    # Parse data
    x_values = []
    y_series = [[] for _ in y_indices]
    for row in lines[4:]:
        if len(row) <= max(all_indices):
            continue
        try:
            x_values.append(float(row[x_index]))
            for i, yi in enumerate(y_indices):
                y_series[i].append(float(row[yi]))
        except ValueError:
            continue

    # Plotting
    plt.figure()
    for ys, yi in zip(y_series, y_indices):
        plt.plot(x_values, ys, marker='o', label=headers[yi])

    plt.title(title)
    plt.xlabel(headers[x_index])
    plt.ylabel(" / ".join(headers[yi] for yi in y_indices))
    plt.legend()
    plt.grid(True)
    plt.tight_layout()

    # Metadata text
    if metadata_lines:
        metadata_text = "\n".join(metadata_lines)
        plt.figtext(0.15, 0.06, metadata_text, wrap=True,
                    horizontalalignment='left', fontsize=8)
        plt.subplots_adjust(bottom=0.2)

    # Save as PDF
    pdf_path = os.path.join(folder, f"{basename}.pdf")
    plt.savefig(pdf_path, bbox_inches="tight")
    print(f"Plot saved to {pdf_path}")

    plt.show()

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 plot.py <foldername> <filename>")
        print("Example: python3 plot.py nash distExp")
        sys.exit(1)

    folder_arg = sys.argv[1]
    file_arg = sys.argv[2]
    plot_from_custom_format(folder_arg, file_arg)
