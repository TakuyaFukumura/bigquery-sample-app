// ダークモード切替機能

document.addEventListener('DOMContentLoaded', function() {
    const darkModeToggle = document.getElementById('darkModeToggle');
    const darkModeIcon = document.getElementById('darkModeIcon');
    const htmlElement = document.documentElement;

    // ダークモード切替ボタンかアイコン要素が存在しない場合は何もしない
    if (!darkModeToggle || !darkModeIcon) {
        return;
    }

    // ローカルストレージからテーマを取得
    const savedTheme = localStorage.getItem('theme') || 'light';
    htmlElement.setAttribute('data-bs-theme', savedTheme);
    updateIcon(savedTheme);

    // トグルボタンのクリックイベント
    darkModeToggle.addEventListener('click', function() {
        const currentTheme = htmlElement.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        htmlElement.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateIcon(newTheme);
    });

    // アイコンの更新
    function updateIcon(theme) {
        if (theme === 'dark') {
            darkModeIcon.className = 'bi bi-sun-fill';
            darkModeToggle.title = 'ライトモードに切替';
        } else {
            darkModeIcon.className = 'bi bi-moon-fill';
            darkModeToggle.title = 'ダークモードに切替';
        }
    }
});
