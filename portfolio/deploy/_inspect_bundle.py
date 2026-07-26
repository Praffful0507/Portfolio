import glob
files = glob.glob("/var/www/frontend/build/static/js/main.*.js")
data = open(files[0], encoding="utf-8", errors="ignore").read()
needle = 'path:"/dashboard/settings"'
print("count", data.count(needle))
idx = 0
n = 0
while n < 10:
    i = data.find(needle, idx)
    if i < 0:
        break
    print(data[max(0, i - 80) : i + 220])
    print("=====")
    idx = i + 1
    n += 1

# Check catch-all near settings
j = data.find('path:"*"')
print("catch-all snippet:", data[j : j + 300] if j >= 0 else "none")
