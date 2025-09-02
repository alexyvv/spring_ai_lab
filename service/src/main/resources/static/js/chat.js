document.addEventListener("DOMContentLoaded", function() {
    // Добавляем стили анимации прямо в head документа
    const style = document.createElement('style');
    style.textContent = `
        @keyframes dotPulse {
            0%, 60%, 100% {
                transform: scale(1);
                opacity: 0.4;
            }
            30% {
                transform: scale(1.3);
                opacity: 1;
            }
        }
        
        .ai-thinking {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 2px 0;
        }
        
        .ai-thinking .dot {
            width: 8px;
            height: 8px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%;
            animation: dotPulse 1.5s infinite ease-in-out;
            box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
        }
        
        .ai-thinking .dot:nth-child(1) {
            animation-delay: 0s;
        }
        
        .ai-thinking .dot:nth-child(2) {
            animation-delay: 0.2s;
        }
        
        .ai-thinking .dot:nth-child(3) {
            animation-delay: 0.4s;
        }
        
        /* Fade in animation for response text */
        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(5px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .ai-response {
            animation: fadeIn 0.3s ease-out;
        }
    `;
    document.head.appendChild(style);
    
    const sendButton = document.getElementById("send-button");
    const sendMvcButton = document.getElementById("send-mvc-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");
    
    // Обработка отправки формы для MVC
    const form = document.querySelector('form.input-area');
    if (form && sendMvcButton) {
        // Обработчик клика на MVC кнопку
        sendMvcButton.addEventListener("click", function(e) {
            const prompt = chatInput.value.trim();
            if (!prompt) {
                e.preventDefault();
                return;
            }
            
            // Добавляем сообщение пользователя в чат (как в streaming)
            const userDiv = document.createElement("div");
            userDiv.className = "message user";
            userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
            messagesContainer.appendChild(userDiv);
            
            // Создаем блок для ответа AI с анимацией
            const aiDiv = document.createElement("div");
            aiDiv.className = "message mentor";
            const img = document.createElement("img");
            img.src = "/images/mentor.png";
            img.alt = "Mentor";
            aiDiv.appendChild(img);
            
            const aiBubble = document.createElement("div");
            aiBubble.className = "bubble";
            
            // Добавляем анимацию загрузки
            const thinkingIndicator = document.createElement("div");
            thinkingIndicator.className = "ai-thinking";
            
            for (let i = 0; i < 3; i++) {
                const dot = document.createElement("span");
                dot.className = "dot";
                thinkingIndicator.appendChild(dot);
            }
            
            aiBubble.appendChild(thinkingIndicator);
            aiDiv.appendChild(aiBubble);
            messagesContainer.appendChild(aiDiv);
            
            // Плавная прокрутка вниз
            messagesContainer.scrollTo({
                top: messagesContainer.scrollHeight,
                behavior: 'smooth'
            });
            
            // Кнопка type="submit" автоматически отправит форму
            // Не нужно вызывать preventDefault или submit вручную
        });
    }

    sendButton.addEventListener("click", function(e) {
        e.preventDefault(); // Предотвращаем отправку формы
        const prompt = chatInput.value;
        if (!prompt) return;
        chatInput.value = "";

        // Добавляем сообщение пользователя в чат
        const userDiv = document.createElement("div");
        userDiv.className = "message user";
        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
        messagesContainer.appendChild(userDiv);

        const pathParts = window.location.pathname.split("/");
        const chatId = pathParts[pathParts.length - 1];
        const url = `/chat-stream/${chatId}?userPrompt=${encodeURIComponent(prompt)}`;

        const eventSource = new EventSource(url);
        let fullText = "";

        // Создаем блок для ответа AI
        const aiDiv = document.createElement("div");
        aiDiv.className = "message mentor";
        // Добавляем изображение ассистента
        const img = document.createElement("img");
        img.src = "/images/mentor.png";
        img.alt = "Mentor";
        aiDiv.appendChild(img);
        
        // Создаем элемент для содержимого, куда будем вставлять ответ
        const aiBubble = document.createElement("div");
        aiBubble.className = "bubble";
        
        // Добавляем элегантную анимацию загрузки
        const thinkingIndicator = document.createElement("div");
        thinkingIndicator.className = "ai-thinking";
        
        // Создаем три точки
        for (let i = 0; i < 3; i++) {
            const dot = document.createElement("span");
            dot.className = "dot";
            thinkingIndicator.appendChild(dot);
        }
        
        aiBubble.appendChild(thinkingIndicator);
        aiDiv.appendChild(aiBubble);
        messagesContainer.appendChild(aiDiv);
        
        // Плавная прокрутка вниз
        messagesContainer.scrollTo({
            top: messagesContainer.scrollHeight,
            behavior: 'smooth'
        });
        
        // Флаг для отслеживания первого полученного токена
        let firstTokenReceived = false;
        
        console.log("Loading placeholder added to chat");

        eventSource.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                let token = data.text;
                
                // При получении первого токена убираем анимацию загрузки
                if (!firstTokenReceived) {
                    firstTokenReceived = true;
                    aiBubble.innerHTML = ""; // Очищаем анимацию
                    aiBubble.classList.add("ai-response"); // Добавляем класс для анимации появления
                }
                
                fullText += token;
                // Преобразуем Markdown в HTML (при условии, что marked.js подключен)
                if (typeof marked !== 'undefined') {
                    aiBubble.innerHTML = marked.parse(fullText);
                } else {
                    aiBubble.textContent = fullText;
                }
                
                // Плавная прокрутка только если близко к низу
                const isNearBottom = messagesContainer.scrollHeight - messagesContainer.scrollTop - messagesContainer.clientHeight < 100;
                if (isNearBottom) {
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }
            } catch (error) {
                console.error("Error parsing SSE data:", error);
            }
        };

        eventSource.onerror = function(e) {
            eventSource.close();
            
            // Перезагружаем страницу только если не получили данных (реальная ошибка)
            if (fullText.length === 0) {
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            }
        };
    });
    
    // Allow sending message with Enter key
    chatInput.addEventListener("keypress", function(e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendButton.click();
        }
    });
});
