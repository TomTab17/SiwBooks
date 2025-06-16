document.addEventListener('DOMContentLoaded', function() {
    const mainTitle = document.querySelector('h1');
    
    if (mainTitle && !mainTitle.dataset.holaProcessed) {
        const text = mainTitle.textContent;
        mainTitle.textContent = ''; 
        Array.from(text).forEach(char => {
            const span = document.createElement('span');
            span.textContent = char;
            if (char === ' ') {
                span.classList.add('hola-space');
            }
            span.classList.add('hola-letter'); 
            mainTitle.appendChild(span);
        });
        mainTitle.dataset.holaProcessed = 'true'; 

        const holaLetters = document.querySelectorAll('h1 .hola-letter');

        function animateHolaOnce() {
            let delay = 0;
            holaLetters.forEach((letter) => {
                setTimeout(() => {
                    letter.classList.add('hola-color');
                }, delay);

                setTimeout(() => {
                    letter.classList.remove('hola-color');
                }, delay + 200); 

                delay += 70; 
            });
        }

        animateHolaOnce();
    }

    const alphabetContainer = document.querySelector('.alphabet-az');
    const bookListItems = Array.from(document.querySelectorAll('.book-list li'))
                               .filter(li => !li.classList.contains('list-header'));
    const bookList = document.querySelector('.book-list');
    const listHeader = document.querySelector('.book-list .list-header');


    function generateAlphabet() {
        if (!alphabetContainer) {
            console.error("Errore: Il div con classe 'alphabet-az' non è stato trovato nel DOM.");
            return;
        }

        for (let i = 65; i <= 90; i++) {
            const letter = String.fromCharCode(i);
            const span = document.createElement('span');
            span.textContent = letter;
            span.dataset.letter = letter;
            alphabetContainer.appendChild(span);
        }
    }

    function sortBooksAlphabetically() {
        const sortedBooks = Array.from(bookListItems).sort((a, b) => {
            const titleA = a.querySelector('.book-link span').textContent.toLowerCase();
            const titleB = b.querySelector('.book-link span').textContent.toLowerCase();
            return titleA.localeCompare(titleB);
        });

        bookListItems.forEach(book => book.remove());
        
        if (listHeader && !bookList.contains(listHeader)) {
            bookList.prepend(listHeader);
        }
        
        sortedBooks.forEach(book => bookList.appendChild(book));
    }

    if (alphabetContainer) {
        alphabetContainer.addEventListener('click', function(event) {
            if (event.target.tagName === 'SPAN' && event.target.dataset.letter) {
                const clickedLetter = event.target.dataset.letter;

                Array.from(alphabetContainer.children).forEach(span => span.classList.remove('active'));
                event.target.classList.add('active');

                let found = false;
                for (let i = 0; i < bookListItems.length; i++) {
                    const bookTitle = bookListItems[i].querySelector('.book-link span').textContent;
                    if (bookTitle.toUpperCase().startsWith(clickedLetter)) {
                        bookListItems[i].scrollIntoView({ behavior: 'smooth', block: 'start' });
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    alert(`Nessun libro trovato con la lettera '${clickedLetter}'.`);
                }
            }
        });
    }

    generateAlphabet();
    sortBooksAlphabetically();
});