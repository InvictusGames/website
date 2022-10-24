/*
*	MADE BY XEMAH
*	https://xemah.com
*
*
**/

var currentPath = location.pathname.replace(/\/$/, '') + location.search;

function findAncestor(el, sel) {

    if ((el.matches || el.matchesSelector).call(el, sel)) {
        return el;
    }

    while ((el = el.parentElement) && !((el.matches || el.matchesSelector).call(el, sel))) ;
    return el;

}

(() => {

    const dropdownElementList = [].slice.call(document.querySelectorAll('[data-bs-toggle="dropdown"]'));
    dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl);
    });

})();

(() => {

    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

})();

(() => {

    const imageElements = document.querySelectorAll('img');
    for (const element of imageElements) {
        element.addEventListener('error', () => {
            element.src = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=';
        });
    }

})();

(() => {

    const playerSearchInput = document.querySelector('#input-playerSearch');
    if (!playerSearchInput) {
        return false;
    }

    const playerSearchDropdown = document.querySelector('#dropdown-playerSearch');
    const playerSearchDropdownToggler = playerSearchDropdown.querySelector('[data-bs-toggle="dropdown"]');
    const playerSearchDropdownMenu = playerSearchDropdown.querySelector('.dropdown-menu');

    let playerSearchDropdownInstance = bootstrap.Dropdown.getInstance(playerSearchDropdownToggler);
    if (!playerSearchDropdownInstance) {
        playerSearchDropdownInstance = new bootstrap.Dropdown(playerSearchDropdownToggler);
    }

    playerSearchDropdown.addEventListener('show.bs.dropdown', (event) => {
        if (playerSearchDropdownMenu && playerSearchDropdownMenu.innerHTML.trim() === '') {
            event.preventDefault();
        }
    });

    let playerSearchTimeout = null;
    playerSearchInput.addEventListener('input', () => {
        playerSearchDropdownInstance.hide();
        if (playerSearchInput.value.length > 0) {

            if (playerSearchTimeout)
                clearTimeout(playerSearchTimeout);

            playerSearchTimeout = setTimeout(async () => {
                const response = await fetch(`/search?query=${playerSearchInput.value}`);
                if (!response) return false;
                const responseJson = await response.json();
                const users = responseJson.users || [];

                if (users.length === 1) {
                    const user = users[0];
                    window.location.href = user.link;
                }

                playerSearchDropdownMenu.innerHTML = users.map((user) => {
                    return `<li>
						<a href="${user.link}" class="dropdown-item">
							<img src="${user.avatar}" alt="${user.name}">
							<span>${user.name}</span>
						</a>
					</li>`;
                }).join('');

                if (users.length > 0)
                    playerSearchDropdownInstance.show();

            }, 1);
        }
    });

})();